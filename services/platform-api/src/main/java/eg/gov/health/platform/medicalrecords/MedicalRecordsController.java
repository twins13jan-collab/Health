package eg.gov.health.platform.medicalrecords;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.clinical.*;
import eg.gov.health.platform.patient.PatientRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Medical records & coding (Phase 4.9). The department's worklist
 *  is the facility's completed encounters: each file needs a signed
 *  clinical note, a final closing summary and ICD-10 coding
 *  (RecordRules) before it may be filed — filing a deficient file is
 *  refused with its gaps named. The primary code must exist in the
 *  national reference set loaded in the platform (ref_icd10_codes);
 *  an invented code is rejected, never stored. */
@RestController @RequestMapping("/api/medical-records")
public class MedicalRecordsController {
  private final EncounterRepository encounters; private final ClinicalNoteRepository notes;
  private final EncounterSummaryRepository summaries; private final EncounterCodingRepository codings;
  private final PatientRepository patients; private final JdbcTemplate jdbc; private final AuditService audit;
  public MedicalRecordsController(EncounterRepository encounters, ClinicalNoteRepository notes,
                                  EncounterSummaryRepository summaries, EncounterCodingRepository codings,
                                  PatientRepository patients, JdbcTemplate jdbc, AuditService audit) {
    this.encounters=encounters; this.notes=notes; this.summaries=summaries; this.codings=codings;
    this.patients=patients; this.jdbc=jdbc; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private Encounter encounter(UUID id, UUID fac) {
    var e = encounters.findById(id).orElseThrow(() -> BizException.notFound("encounter_not_found","اللقاء غير موجود"));
    if (!fac.equals(e.getFacilityId())) throw BizException.notFound("encounter_not_found","اللقاء غير موجود");
    return e;
  }
  private List<String> deficienciesOf(Encounter e) {
    boolean signed = notes.findByEncounterIdOrderByCreatedAtAsc(e.getId()).stream()
        .anyMatch(n -> "signed".equals(n.getStatus()) || "amended".equals(n.getStatus()));
    var sum = summaries.findByEncounterId(e.getId());
    boolean coded = codings.findByEncounterId(e.getId()).isPresent();
    return RecordRules.deficiencies(signed, sum.map(EncounterSummary::getStatus).orElse(null), coded);
  }

  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal(); UUID fac = UUID.fromString(p.facilityId());
    var now = OffsetDateTime.now();
    var today = LocalDate.now(ZoneId.of("Africa/Cairo"));
    List<Map<String,Object>> rows = new ArrayList<>();
    long ready = 0, overdue = 0, missingNotes = 0, missingSummary = 0, missingCoding = 0, filedToday = 0;
    for (var e : encounters.findByFacilityIdOrderByArrivedAtDesc(fac)) {
      if ("closed".equals(e.getStatus())) {
        if (e.getUpdatedAt()!=null && e.getUpdatedAt().atZoneSameInstant(ZoneId.of("Africa/Cairo")).toLocalDate().equals(today)) filedToday++;
        continue;
      }
      if (!"completed".equals(e.getStatus())) continue;
      var defs = deficienciesOf(e);
      boolean isReady = defs.isEmpty();
      boolean isOverdue = RecordRules.filingOverdue(e.getUpdatedAt(), now);
      if (isReady) ready++;
      if (isOverdue) overdue++;
      if (defs.contains("clinical_note_missing")) missingNotes++;
      if (defs.contains("discharge_summary_missing") || defs.contains("discharge_summary_not_final")) missingSummary++;
      if (defs.contains("icd10_coding_missing")) missingCoding++;
      Map<String,Object> r = new LinkedHashMap<>();
      r.put("encounter_id", e.getId()); r.put("patient_id", e.getPatientId());
      r.put("patient_name", patients.findById(e.getPatientId()).map(pt -> pt.getFullName()).orElse(""));
      r.put("type", e.getType()); r.put("chief_complaint", e.getChiefComplaint());
      r.put("completed_at", e.getUpdatedAt());
      r.put("days_waiting", e.getUpdatedAt()==null ? 0 : Duration.between(e.getUpdatedAt(), now).toDays());
      r.put("deficiencies", defs); r.put("ready", isReady); r.put("overdue", isOverdue);
      r.put("attention_rank", RecordRules.attentionRank(isReady, isOverdue));
      summaries.findByEncounterId(e.getId()).ifPresent(sm -> { r.put("summary_status", sm.getStatus()); r.put("summary_text", sm.getSummaryText()); });
      codings.findByEncounterId(e.getId()).ifPresent(cd -> { r.put("primary_icd10", cd.getPrimaryIcd10()); r.put("secondary_icd10", cd.getSecondaryIcd10()); });
      rows.add(r);
    }
    rows.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("attention_rank"))
        .thenComparing(r -> (OffsetDateTime) r.get("completed_at"), Comparator.nullsLast(Comparator.naturalOrder())));
    Map<String,Object> summary = new LinkedHashMap<>();
    summary.put("awaiting_filing", rows.size()); summary.put("ready_to_file", ready);
    summary.put("deficient", rows.size() - ready); summary.put("overdue", overdue);
    summary.put("missing_notes", missingNotes); summary.put("missing_summary", missingSummary);
    summary.put("missing_coding", missingCoding); summary.put("filed_today", filedToday);
    Map<String,Object> out = new LinkedHashMap<>();
    out.put("ok", true); out.put("board", rows); out.put("count", rows.size()); out.put("summary", summary);
    return out;
  }

  public record SummaryBody(String text) {}
  @PostMapping("/{encounterId}/summary")
  Map<String,Object> saveSummary(@PathVariable UUID encounterId, @RequestBody SummaryBody r) {
    var p = principal(); UUID fac = UUID.fromString(p.facilityId());
    var e = encounter(encounterId, fac);
    if (r.text()==null || r.text().isBlank()) throw BizException.badRequest("empty_summary","نص الملخص مطلوب");
    var existing = summaries.findByEncounterId(encounterId);
    EncounterSummary sm;
    if (existing.isPresent()) {
      sm = existing.get();
      if ("final".equals(sm.getStatus())) throw BizException.conflict("summary_already_final","الملخص النهائي هو نسخة السجل — لا يُحرر، والتصحيح يكون بملحق موثق");
      sm.revise(r.text(), p.email());
    } else {
      sm = new EncounterSummary(fac, p.governorate(), encounterId, e.getPatientId(), r.text(), p.email());
    }
    summaries.save(sm);
    audit.record("medical_record.summary_saved","encounter",encounterId.toString(),"{}");
    return Map.of("ok",true,"summary",sm,"deficiencies",deficienciesOf(e));
  }
  @PostMapping("/{encounterId}/summary/finalize")
  Map<String,Object> finalizeSummary(@PathVariable UUID encounterId) {
    var p = principal(); UUID fac = UUID.fromString(p.facilityId());
    var e = encounter(encounterId, fac);
    var sm = summaries.findByEncounterId(encounterId)
        .orElseThrow(() -> BizException.notFound("summary_not_found","لا يوجد ملخص لهذا الملف بعد"));
    if ("final".equals(sm.getStatus())) throw BizException.conflict("summary_already_final","الملخص معتمد نهائيًا بالفعل");
    sm.finalizeSummary(); summaries.save(sm);
    audit.record("medical_record.summary_finalized","encounter",encounterId.toString(),"{}");
    return Map.of("ok",true,"summary",sm,"deficiencies",deficienciesOf(e));
  }

  public record CodingBody(String primaryIcd10, String secondaryIcd10) {}
  @PostMapping("/{encounterId}/coding")
  Map<String,Object> saveCoding(@PathVariable UUID encounterId, @RequestBody CodingBody r) {
    var p = principal(); UUID fac = UUID.fromString(p.facilityId());
    var e = encounter(encounterId, fac);
    String primary = r.primaryIcd10()==null ? null : r.primaryIcd10().trim().toUpperCase();
    if (!RecordRules.icdFormatValid(primary))
      throw BizException.badRequest("invalid_icd10_format","صيغة كود ICD-10 غير صحيحة (مثال: J18.9)");
    Integer known = jdbc.queryForObject("SELECT count(*) FROM ref_icd10_codes WHERE code = ?", Integer.class, primary);
    if (known==null || known==0)
      throw BizException.badRequest("unknown_icd10_code","الكود غير موجود في المجموعة المرجعية الوطنية المحمّلة في المنظومة — لا يُحفظ كود مخترع");
    if (r.secondaryIcd10()!=null && !r.secondaryIcd10().isBlank())
      for (String c : r.secondaryIcd10().split(","))
        if (!RecordRules.icdFormatValid(c)) throw BizException.badRequest("invalid_icd10_format","صيغة كود ثانوي غير صحيحة: "+c.trim());
    var existing = codings.findByEncounterId(encounterId);
    EncounterCoding cd;
    if (existing.isPresent()) { cd = existing.get(); cd.revise(primary, r.secondaryIcd10(), p.email()); }
    else cd = new EncounterCoding(fac, p.governorate(), encounterId, e.getPatientId(), primary, r.secondaryIcd10(), p.email());
    codings.save(cd);
    audit.record("medical_record.coding_saved","encounter",encounterId.toString(),"{}");
    return Map.of("ok",true,"coding",cd,"deficiencies",deficienciesOf(e));
  }

  @PostMapping("/{encounterId}/file")
  Map<String,Object> file(@PathVariable UUID encounterId) {
    var p = principal(); UUID fac = UUID.fromString(p.facilityId());
    var e = encounter(encounterId, fac);
    if (!"completed".equals(e.getStatus()))
      throw BizException.conflict("encounter_not_completed","الأرشفة بعد اكتمال اللقاء فقط — حالته الحالية: "+e.getStatus());
    var defs = deficienciesOf(e);
    if (!defs.isEmpty())
      throw BizException.conflict("file_incomplete","لا يُقفل ملف بنواقص موثقة: "+String.join(", ", defs));
    e.setStatus("closed"); encounters.save(e);
    audit.record("medical_record.filed","encounter",encounterId.toString(),"{}");
    return Map.of("ok",true,"encounter_id",encounterId,"status","closed");
  }
}
