package eg.gov.health.platform.ledger;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
/** General ledger, closing & bank reconciliation (cycle 229). Every
 *  journal is balanced, sourced, documented and period-bound; posted
 *  journals are immutable and corrected only by reversal; the trial
 *  balance is reproduced from posted journals; balances trace from the
 *  ledger down to the subledger; a closed period reopens only with a
 *  finance-manager approval, a reason and an audit trail. */
@RestController @RequestMapping("/api/ledger")
public class LedgerController {
  private final ChartOfAccountRepository accounts; private final AccountingPeriodRepository periods;
  private final JournalRepository journals; private final JournalLineRepository lines;
  private final SubledgerLinkRepository subledgers; private final BankStatementRepository statements;
  private final BankTransactionRepository bankTxs; private final CloseChecklistRepository checklists;
  private final AuditService audit;
  public LedgerController(ChartOfAccountRepository accounts, AccountingPeriodRepository periods, JournalRepository journals,
                          JournalLineRepository lines, SubledgerLinkRepository subledgers, BankStatementRepository statements,
                          BankTransactionRepository bankTxs, CloseChecklistRepository checklists, AuditService audit) {
    this.accounts=accounts; this.periods=periods; this.journals=journals; this.lines=lines;
    this.subledgers=subledgers; this.statements=statements; this.bankTxs=bankTxs; this.checklists=checklists; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private AccountingPeriod period(String key) {
    return periods.findByPeriodKey(key).orElseThrow(() -> BizException.notFound("period_not_found","الفترة غير موجودة — افتحها أولًا"));
  }
  private Journal journal(UUID id) {
    return journals.findById(id).orElseThrow(() -> BizException.notFound("journal_not_found","القيد غير موجود"));
  }

  public record AccountRequest(String accountCode, String name, String classification, String postingRules, String owner) {}
  @PostMapping("/accounts")
  Map<String,Object> account(@RequestBody AccountRequest r) {
    var p = principal();
    if (r.accountCode()==null||r.name()==null||r.classification()==null)
      throw BizException.badRequest("invalid_account","الحساب والتصنيف والجهة المالكة مطلوبة");
    var a = accounts.save(new ChartOfAccount(UUID.fromString(p.facilityId()), p.governorate(), r.accountCode(), r.name(),
      r.classification(), r.postingRules(), r.owner()));
    audit.record("ledger.account.created","chart_of_account",a.getId().toString(),"{}");
    return Map.of("ok",true,"account",a);
  }
  @PostMapping("/periods/{key}/open")
  Map<String,Object> openPeriod(@PathVariable String key) {
    var p = principal();
    var per = periods.save(new AccountingPeriod(UUID.fromString(p.facilityId()), p.governorate(), key));
    audit.record("ledger.period.opened","accounting_period",per.getId().toString(),"{}");
    return Map.of("ok",true,"period",per);
  }

  public record LineDto(String accountCode, BigDecimal debit, BigDecimal credit, String costCenter) {}
  public record JournalRequest(String sourceSystem, String sourceRef, String periodKey, String currency,
                               String fxRateSource, LocalDate fxRateDate, List<LineDto> lines,
                               String subledgerType, String subledgerRef) {}
  @PostMapping("/journals")
  Map<String,Object> journal(@RequestBody JournalRequest r) {
    var p = principal();
    if (r.sourceSystem()==null||r.periodKey()==null||r.lines()==null||r.lines().isEmpty())
      throw BizException.badRequest("invalid_journal","كل قيد له مصدر ومستند ومستخدم وفترة وسطور");
    if (!"EGP".equals(r.currency()==null?"EGP":r.currency()) && (r.fxRateSource()==null||r.fxRateDate()==null))
      throw BizException.badRequest("fx_source_required","اختلاف العملة يحفظ مصدر سعر الصرف وتاريخه");
    var per = period(r.periodKey());
    if (!LedgerRules.periodPostable(per.getStatus()))
      throw BizException.conflict("period_closed","الفترة مغلقة — الترحيل إليها ممنوع");
    var j = journals.save(new Journal(UUID.fromString(p.facilityId()), p.governorate(), r.sourceSystem(), r.sourceRef(),
      r.periodKey(), r.currency(), r.fxRateSource(), r.fxRateDate(), null, p.email()));
    for (var l : r.lines())
      lines.save(new JournalLine(j.getFacilityId(), p.governorate(), j.getId(), l.accountCode(), l.debit(), l.credit(), l.costCenter()));
    if (r.subledgerType()!=null && r.subledgerRef()!=null)
      subledgers.save(new SubledgerLink(j.getFacilityId(), p.governorate(), j.getId(), r.subledgerType(), r.subledgerRef()));
    audit.record("ledger.journal.drafted","journal",j.getId().toString(),"{}");
    return Map.of("ok",true,"journal",j,"status","draft");
  }
  @PostMapping("/journals/{id}/post")
  Map<String,Object> post(@PathVariable UUID id) {
    var p = principal(); var j = journal(id);
    if (!LedgerRules.mayPost(p.role()))
      throw BizException.forbidden("الترحيل والإقفال للمدير المالي — المستخدم التشغيلي ينشئ المعاملة المصدرية فقط");
    if (!"draft".equals(j.getStatus())) throw BizException.conflict("not_draft","القيد ليس مسودة");
    var per = period(j.getPeriodKey());
    if (!LedgerRules.periodPostable(per.getStatus())) throw BizException.conflict("period_closed","الفترة مغلقة");
    var ls = lines.findByJournalIdOrderByCreatedAtAsc(id);
    List<BigDecimal[]> pairs = ls.stream().map(l -> new BigDecimal[]{l.getDebit(), l.getCredit()}).toList();
    if (!LedgerRules.balanced(pairs))
      throw BizException.badRequest("unbalanced_journal","القيد غير المتوازن يرفض — مجموع المدين يجب أن يساوي الدائن");
    j.post(p.email()); journals.save(j);
    audit.record("ledger.journal.posted","journal",id.toString(),"{}");
    return Map.of("ok",true,"journal",j,"immutable",true,"note","posted journals are never edited — correction is a reversing journal");
  }
  /** Correction of a posted journal happens ONLY via a reversing journal. */
  @PostMapping("/journals/{id}/reverse")
  Map<String,Object> reverse(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var orig = journal(id);
    if (!LedgerRules.mayPost(p.role()))
      throw BizException.forbidden("عكس القيد للمدير المالي");
    if (!"posted".equals(orig.getStatus())) throw BizException.conflict("not_posted","العكس لقيود مرحلة فقط");
    if (body.get("reason")==null||body.get("reason").isBlank())
      throw BizException.badRequest("reason_required","القيد العكسي يتطلب سببًا موثقًا");
    var per = period(orig.getPeriodKey());
    if (!LedgerRules.periodPostable(per.getStatus())) throw BizException.conflict("period_closed","فترة القيد الأصلي مغلقة");
    var rev = journals.save(new Journal(orig.getFacilityId(), p.governorate(), "reversal", orig.getSourceRef(),
      orig.getPeriodKey(), "EGP", null, null, id, p.email()));
    for (var l : lines.findByJournalIdOrderByCreatedAtAsc(id))
      lines.save(new JournalLine(orig.getFacilityId(), p.governorate(), rev.getId(), l.getAccountCode(), l.getCredit(), l.getDebit(), null));
    rev.post(p.email()); journals.save(rev);
    orig.markReversed(); journals.save(orig);
    audit.record("ledger.journal.reversed","journal",rev.getId().toString(),"{}");
    return Map.of("ok",true,"reversal",rev,"original_status","reversed");
  }
  @GetMapping("/trial-balance")
  Map<String,Object> trialBalance(@RequestParam String periodKey) {
    Map<String, BigDecimal[]> tb = new TreeMap<>();
    // Posted journals AND reversed originals both stay in the books: the
    // reversal offsets the original, so accounts net correctly.
    for (var j : journals.findByPeriodKeyAndStatusIn(periodKey, List.of("posted", "reversed"))) {
      for (var l : lines.findByJournalIdOrderByCreatedAtAsc(j.getId())) {
        var cell = tb.computeIfAbsent(l.getAccountCode(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        cell[0] = cell[0].add(l.getDebit()); cell[1] = cell[1].add(l.getCredit());
      }
    }
    BigDecimal td = BigDecimal.ZERO, tc = BigDecimal.ZERO;
    List<Map<String,Object>> rows = new ArrayList<>();
    for (var e : tb.entrySet()) {
      rows.add(Map.of("account", e.getKey(), "debit", e.getValue()[0], "credit", e.getValue()[1],
        "balance", e.getValue()[0].subtract(e.getValue()[1])));
      td = td.add(e.getValue()[0]); tc = tc.add(e.getValue()[1]);
    }
    return Map.of("ok",true,"period",periodKey,"rows",rows,"total_debit",td,"total_credit",tc,
      "balanced", td.compareTo(tc)==0,"reproduced_from","posted journals only");
  }
  @PostMapping("/periods/{key}/close")
  Map<String,Object> closePeriod(@PathVariable String key) {
    var p = principal();
    if (!LedgerRules.mayPost(p.role())) throw BizException.forbidden("الإقفال للمدير المالي");
    var per = period(key);
    long drafts = journals.findByPeriodKeyAndStatus(key, "draft").size();
    if (drafts > 0) throw BizException.conflict("pending_entries","توجد قيود معلقة (مسودات) — الإقفال يتطلب صفر قيود معلقة");
    per.close(p.email()); periods.save(per);
    var cl = checklists.save(new CloseChecklist(per.getFacilityId(), p.governorate(), key, 0, p.email()));
    cl.setStatus("done"); checklists.save(cl);
    audit.record("ledger.period.closed","accounting_period",per.getId().toString(),"{}");
    return Map.of("ok",true,"period",per);
  }
  @PostMapping("/periods/{key}/reopen")
  Map<String,Object> reopenPeriod(@PathVariable String key, @RequestBody Map<String,String> body) {
    var p = principal();
    if (!LedgerRules.mayPost(p.role())) throw BizException.forbidden("إعادة فتح فترة مغلقة للمدير المالي بموافقة وسبب");
    if (body.get("reason")==null||body.get("reason").isBlank())
      throw BizException.badRequest("reason_required","إعادة فتح فترة مغلقة تحتاج موافقة وسببًا وسجل أثر");
    var per = period(key);
    if (!"closed".equals(per.getStatus())) throw BizException.conflict("not_closed","الفترة ليست مغلقة");
    per.reopen(p.email(), body.get("reason")); periods.save(per);
    audit.record("ledger.period.reopened","accounting_period",per.getId().toString(),"{}");
    return Map.of("ok",true,"period",per,"audit_trail","reopen recorded with approver and reason");
  }

  public record StatementRequest(String accountCode, String statementRef, String periodKey) {}
  @PostMapping("/bank-statements")
  Map<String,Object> statement(@RequestBody StatementRequest r) {
    var p = principal();
    var s = statements.save(new BankStatement(UUID.fromString(p.facilityId()), p.governorate(), r.accountCode(), r.statementRef(), r.periodKey()));
    audit.record("ledger.bank.statement_imported","bank_statement",s.getId().toString(),"{}");
    return Map.of("ok",true,"statement",s);
  }
  public record BankTxRequest(String reference, LocalDate txDate, BigDecimal amount) {}
  @PostMapping("/bank-statements/{id}/transactions")
  Map<String,Object> bankTx(@PathVariable UUID id, @RequestBody BankTxRequest r) {
    var p = principal();
    var s = statements.findById(id).orElseThrow(() -> BizException.notFound("statement_not_found","الكشف غير موجود"));
    var t = bankTxs.save(new BankTransaction(s.getFacilityId(), p.governorate(), id, r.reference(), r.txDate(), r.amount()));
    audit.record("ledger.bank.tx_added","bank_transaction",t.getId().toString(),"{}");
    return Map.of("ok",true,"transaction",t,
      "note", r.reference()==null ? "no reference — this movement stays unmatched by rule" : "");
  }
  @PostMapping("/bank-transactions/{id}/match")
  Map<String,Object> match(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = bankTxs.findById(id).orElseThrow(() -> BizException.notFound("tx_not_found","الحركة غير موجودة"));
    UUID jid = body.get("journalId")==null ? null : UUID.fromString(body.get("journalId"));
    var j = jid==null ? null : journal(jid);
    BigDecimal jAmount = BigDecimal.ZERO;
    if (j != null) for (var l : lines.findByJournalIdOrderByCreatedAtAsc(jid)) jAmount = jAmount.add(l.getDebit());
    if (j == null || !"posted".equals(j.getStatus()) || !LedgerRules.matchable(t.getReference(), j.getSourceRef(), t.getAmount(), jAmount))
      throw BizException.conflict("not_matchable","المطابقة تتطلب مرجعًا متطابقًا وقيمة متساوية وقيدًا مرحلًا — الحركة بلا مرجع تبقى غير مطابقة");
    t.match(jid); bankTxs.save(t);
    audit.record("ledger.bank.matched","bank_transaction",id.toString(),"{}");
    return Map.of("ok",true,"transaction",t);
  }
}
