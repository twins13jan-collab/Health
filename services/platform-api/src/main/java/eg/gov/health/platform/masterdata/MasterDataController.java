package eg.gov.health.platform.masterdata;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
/** Master data & reference catalogues (cycle 235). Every catalogue has
 *  an owner, a source and a version; local codes map to unified codes
 *  with a documented equivalence; unknown codes go to review and are
 *  never guessed; deprecated codes leave historical records readable
 *  but cannot enter new transactions without a governance-approved
 *  exception; stewardship decisions belong to governance, not to the
 *  requesting facility. */
@RestController @RequestMapping("/api/master-data")
public class MasterDataController {
  private final MasterEntityRepository entities; private final ReferenceCodeRepository codes;
  private final MappingSetRepository mappings; private final StewardshipCaseRepository cases;
  private final DataQualityRuleRepository quality; private final AuditService audit;
  public MasterDataController(MasterEntityRepository entities, ReferenceCodeRepository codes, MappingSetRepository mappings,
                              StewardshipCaseRepository cases, DataQualityRuleRepository quality, AuditService audit) {
    this.entities=entities; this.codes=codes; this.mappings=mappings; this.cases=cases; this.quality=quality; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private boolean hasApprovedCase(String targetRef, String changeType) {
    return !cases.findByTargetRefAndChangeTypeAndDecision(targetRef, changeType, "approved").isEmpty();
  }

  public record EntityRequest(String entityType, String code, String nameAr, String nameEn, String ownerDomain) {}
  @PostMapping("/entities")
  Map<String,Object> entity(@RequestBody EntityRequest r) {
    var p = principal();
    if (r.entityType()==null||r.code()==null||r.ownerDomain()==null)
      throw BizException.badRequest("invalid_entity","النوع والرمز ومالك المجال مطلوبة — كل كتالوج له مالك ومصدر وإصدار");
    var e = entities.save(new MasterEntity(UUID.fromString(p.facilityId()), p.governorate(), r.entityType(), r.code(),
      r.nameAr(), r.nameEn(), r.ownerDomain()));
    audit.record("masterdata.entity.created","master_entity",e.getId().toString(),"{}");
    return Map.of("ok",true,"entity",e);
  }

  public record CodeRequest(String codeSystem, String code, String description, String domain, Boolean national, Boolean known) {}
  @PostMapping("/codes")
  Map<String,Object> code(@RequestBody CodeRequest r) {
    var p = principal();
    if (r.codeSystem()==null||r.code()==null) throw BizException.badRequest("invalid_code","نظام الترميز والرمز مطلوبان");
    // An unknown code is registered as unknown_review — never silently accepted as valid.
    String status = Boolean.FALSE.equals(r.known()) ? "unknown_review" : "active";
    var c = codes.save(new ReferenceCode(UUID.fromString(p.facilityId()), p.governorate(), r.codeSystem(), r.code(),
      r.description(), r.domain(), !Boolean.FALSE.equals(r.national()), status));
    audit.record("masterdata.code.registered","reference_code",c.getId().toString(),"{}");
    return Map.of("ok",true,"code",c,
      "note", "unknown_review".equals(status) ? "unknown code queued for stewardship review — never mapped by guessing" : "");
  }
  @GetMapping("/codes/check-usage")
  Map<String,Object> checkUsage(@RequestParam String codeSystem, @RequestParam String code) {
    var c = codes.findByCodeSystemAndCode(codeSystem, code).orElse(null);
    if (c == null) return Map.of("ok",true,"found",false,"usable",false,"status","unknown_review",
      "note","unregistered code is treated as unknown and routed to review");
    boolean exception = hasApprovedCase(codeSystem + ":" + code, "exception_use");
    boolean usable = MasterDataRules.usableInNewTransaction(c.getStatus(), exception);
    return Map.of("ok",true,"found",true,"status",c.getStatus(),"usable_in_new_transaction",usable,
      "approved_exception",exception,
      "historical_readable",true,
      "note","deprecated codes stay readable in historical records; new use needs an approved exception");
  }
  @PostMapping("/codes/{id}/deprecate")
  Map<String,Object> deprecate(@PathVariable UUID id) {
    var c = codes.findById(id).orElseThrow(() -> BizException.notFound("code_not_found","الرمز غير موجود"));
    if (!hasApprovedCase(c.getCode(), "deprecate") && !MasterDataRules.mayDecideStewardship(principal().role()))
      throw BizException.forbidden("إيقاف رمز موحد قرار حوكمة عبر حالة إشراف معتمدة");
    c.setStatus("deprecated"); codes.save(c);
    audit.record("masterdata.code.deprecated","reference_code",id.toString(),"{}");
    return Map.of("ok",true,"code",c,"note","the code remains in historical records — deprecation never deletes");
  }

  public record MappingRequest(String localCode, String standardCode, String equivalence, String conversionReason, Boolean stewardshipApproved) {}
  @PostMapping("/mappings")
  Map<String,Object> mapping(@RequestBody MappingRequest r) {
    var p = principal();
    if (r.localCode()==null||r.standardCode()==null||r.conversionReason()==null)
      throw BizException.badRequest("invalid_mapping","التحويل يتطلب رمزًا محليًا وموحدًا وسبب تحويل موثقًا");
    var local = codes.findByCodeSystemAndCode("LOCAL", r.localCode()).orElse(null);
    String localStatus = local==null ? "unknown_review" : local.getStatus();
    boolean approved = Boolean.TRUE.equals(r.stewardshipApproved())
        || hasApprovedCase(r.localCode(), "map_unknown");
    if (!MasterDataRules.mappingAllowed(localStatus, approved))
      throw BizException.conflict("unknown_code_unmapped","الرمز غير المعروف يذهب للمراجعة ولا يحوَّل تخمينًا — يلزم اعتماد إشراف");
    var m = mappings.save(new MappingSet(UUID.fromString(p.facilityId()), p.governorate(), r.localCode(),
      r.standardCode(), r.equivalence(), r.conversionReason()));
    audit.record("masterdata.mapping.created","mapping_set",m.getId().toString(),"{}");
    return Map.of("ok",true,"mapping",m,"traceable",true);
  }

  public record CaseRequest(String changeType, String targetRef, String justification) {}
  @PostMapping("/stewardship")
  Map<String,Object> stewardship(@RequestBody CaseRequest r) {
    var p = principal();
    if (r.changeType()==null||r.targetRef()==null||r.justification()==null||r.justification().isBlank())
      throw BizException.badRequest("invalid_case","طلب الإضافة/التعديل/الإيقاف يتطلب مبررًا موثقًا");
    var c = cases.save(new StewardshipCase(UUID.fromString(p.facilityId()), p.governorate(), r.changeType(), r.targetRef(),
      r.justification(), p.email()));
    audit.record("masterdata.stewardship.opened","stewardship_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @PostMapping("/stewardship/{id}/decide")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal();
    var c = cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة الإشراف غير موجودة"));
    // Governance approves unified codes — the requesting facility never approves its own change.
    if (!MasterDataRules.mayDecideStewardship(p.role()))
      throw BizException.forbidden("قرارات الإشراف للحوكمة — المنشأة تدير قيمها المحلية المسموحة ولا تغير القاموس القومي");
    String decision = body.get("decision");
    if (!("approved".equals(decision)||"rejected".equals(decision)))
      throw BizException.badRequest("invalid_decision","القرار: اعتماد أو رفض");
    c.decide(decision, p.email()); cases.save(c);
    audit.record("masterdata.stewardship.decided","stewardship_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c);
  }

  public record QualityRequest(String ruleType, String target, String expression, BigDecimal threshold,
                               BigDecimal unknownRate, BigDecimal duplicateRate, BigDecimal conflictRate) {}
  @PostMapping("/quality-rules")
  Map<String,Object> quality(@RequestBody QualityRequest r) {
    var p = principal();
    if (r.ruleType()==null||r.target()==null) throw BizException.badRequest("invalid_rule","نوع القاعدة وهدفها مطلوبان");
    var q = quality.save(new DataQualityRule(UUID.fromString(p.facilityId()), p.governorate(), r.ruleType(), r.target(),
      r.expression(), r.threshold(), r.unknownRate(), r.duplicateRate(), r.conflictRate()));
    audit.record("masterdata.quality.measured","data_quality_rule",q.getId().toString(),"{}");
    return Map.of("ok",true,"rule",q,"measures","unknown, duplicate and conflict rates are measured, not assumed");
  }
  @GetMapping("/codes/review-queue")
  Map<String,Object> reviewQueue() {
    var rows = codes.findByStatusOrderByCreatedAtAsc("unknown_review");
    return Map.of("ok",true,"codes",rows,"count",rows.size());
  }
}
