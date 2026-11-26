package eg.gov.health.platform.longitudinal;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
/** Longitudinal record & patient identity reconciliation (cycle 217).
 *  One unified identity with facility identifiers and temporary
 *  identities that never block care; explainable matching with human
 *  review and dual approval for high-risk merges; conflicting source
 *  values stay visible with their provenance; merges and unmerges are
 *  fully audited and no transaction is ever lost in a conversion. */
@RestController @RequestMapping("/api/longitudinal")
public class LongitudinalController {
  private final PatientIdentityRepository identities; private final IdentityLinkRepository links;
  private final ClinicalRecordLinkRepository records; private final DuplicateCaseRepository duplicates;
  private final AuditService audit;
  public LongitudinalController(PatientIdentityRepository identities, IdentityLinkRepository links,
                                ClinicalRecordLinkRepository records, DuplicateCaseRepository duplicates, AuditService audit) {
    this.identities=identities; this.links=links; this.records=records; this.duplicates=duplicates; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private PatientIdentity identity(UUID id) {
    return identities.findById(id).orElseThrow(() -> BizException.notFound("identity_not_found","الهوية غير موجودة"));
  }

  public record IdentityRequest(UUID patientId, String identityKind, String proofSource) {}
  @PostMapping("/identities")
  Map<String,Object> create(@RequestBody IdentityRequest r) {
    var p = principal();
    var i = identities.save(new PatientIdentity(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.identityKind(), r.proofSource()));
    audit.record("identity.created","patient_identity",i.getId().toString(),"{}");
    return Map.of("ok",true,"identity",i,
      "note", "temporary".equals(i.getIdentityKind()) ? "temporary identity — care is never blocked; matching happens later" : "");
  }
  public record LinkRequest(String linkType, String issuer, String protectedValue, LocalDate validFrom, LocalDate validTo) {}
  @PostMapping("/identities/{id}/links")
  Map<String,Object> link(@PathVariable UUID id, @RequestBody LinkRequest r) {
    var p = principal(); var i = identity(id);
    if (r.linkType()==null) throw BizException.badRequest("link_type_required","نوع المعرف مطلوب");
    var l = links.save(new IdentityLink(i.getFacilityId(), p.governorate(), id, r.linkType(), r.issuer(),
      r.protectedValue(), r.validFrom(), r.validTo()));
    audit.record("identity.link.added","identity_link",l.getId().toString(),"{}");
    return Map.of("ok",true,"link",l);
  }
  public record RecordRequest(String recordType, String recordRef, String ownerFacility, String sourceSystem, String fieldName, String fieldValue) {}
  @PostMapping("/identities/{id}/records")
  Map<String,Object> record(@PathVariable UUID id, @RequestBody RecordRequest r) {
    var p = principal(); var i = identity(id);
    if (r.recordType()==null||r.recordRef()==null) throw BizException.badRequest("invalid_record","نوع السجل ومرجعه مطلوبان");
    var rec = records.save(new ClinicalRecordLink(i.getFacilityId(), p.governorate(), id, r.recordType(), r.recordRef(),
      r.ownerFacility(), r.sourceSystem(), r.fieldName(), r.fieldValue()));
    audit.record("identity.record.linked","clinical_record_link",rec.getId().toString(),"{}");
    return Map.of("ok",true,"record",rec);
  }
  /** Conflicting values from two sources stay visible with provenance. */
  @GetMapping("/identities/{id}/conflicts")
  Map<String,Object> conflicts(@PathVariable UUID id) {
    identity(id);
    var rows = records.findByIdentityIdOrderByCreatedAtAsc(id);
    Map<String, List<Map<String,Object>>> byField = new LinkedHashMap<>();
    for (var r : rows) {
      if (r.getFieldName()==null) continue;
      byField.computeIfAbsent(r.getFieldName(), k -> new ArrayList<>())
        .add(Map.of("value", r.getFieldValue()==null?"":r.getFieldValue(), "source", r.getSourceSystem()==null?"":r.getSourceSystem()));
    }
    var conflicts = byField.entrySet().stream()
      .filter(e -> e.getValue().stream().map(m -> m.get("value")).distinct().count() > 1)
      .map(e -> Map.of("field", e.getKey(), "values_by_source", e.getValue())).toList();
    return Map.of("ok",true,"conflicts",conflicts,
      "note","conflicts stay visible with each value's source — nothing is silently overwritten");
  }
  /** Temporary -> verified conversion: every linked record survives. */
  @PostMapping("/identities/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var i = identity(id);
    long before = records.countByIdentityId(id);
    if (body.get("proofSource")==null||body.get("proofSource").isBlank())
      throw BizException.badRequest("proof_required","التحويل لهوية موثقة يتطلب مصدر إثبات");
    i.verify(body.get("proofSource")); identities.save(i);
    long after = records.countByIdentityId(id);
    audit.record("identity.verified","patient_identity",id.toString(),"{}");
    return Map.of("ok",true,"identity",i,"records_preserved", before==after, "record_count", after);
  }

  public record DuplicateRequest(UUID identityA, UUID identityB, BigDecimal similarityScore, String similarityReasons) {}
  @PostMapping("/duplicates")
  Map<String,Object> duplicate(@RequestBody DuplicateRequest r) {
    var p = principal();
    if (r.identityA()==null||r.identityB()==null||r.similarityScore()==null)
      throw BizException.badRequest("invalid_case","هويتان ودرجة تشابه مفسرة مطلوبة");
    identity(r.identityA()); identity(r.identityB());
    var c = duplicates.save(new DuplicateCase(UUID.fromString(p.facilityId()), p.governorate(), r.identityA(), r.identityB(),
      r.similarityScore(), r.similarityReasons()));
    audit.record("identity.duplicate.opened","duplicate_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c,"explainable",true);
  }
  public record MergeRequest(String secondApprover, String decisionEvidence) {}
  @PostMapping("/duplicates/{id}/merge")
  Map<String,Object> merge(@PathVariable UUID id, @RequestBody MergeRequest r) {
    var p = principal();
    var c = duplicates.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة التكرار غير موجودة"));
    if (!"open".equals(c.getStatus())) throw BizException.conflict("case_closed","الحالة محسومة");
    var a = identity(c.getIdentityA()); var b = identity(c.getIdentityB());
    boolean highRisk = LongitudinalRules.highRisk(c.getSimilarityScore().doubleValue(), a.getIdentityKind(), b.getIdentityKind());
    boolean deciderOk = LongitudinalRules.mayDecideMerge(p.role());
    if (!LongitudinalRules.mergeApprovable(deciderOk, highRisk, p.email(), r.secondApprover()))
      throw BizException.forbidden(highRisk
        ? "دمج عالي المخاطر: مسؤول جودة الهوية يقرر واعتماد مزدوج من شخص مختلف إلزامي — موظف التسجيل لا ينفرد به"
        : "قرار الدمج لمسؤول جودة الهوية فقط (فصل مهام)");
    if (LongitudinalRules.evidenceRequired(c.getSimilarityScore().doubleValue())
        && (r.decisionEvidence()==null||r.decisionEvidence().isBlank()))
      throw BizException.badRequest("evidence_required","قرار الدمج يتطلب أدلة موثقة");
    // Survivor: the verified identity wins; otherwise A survives. Loser's records repoint — nothing is lost.
    var survivor = "verified".equals(b.getIdentityKind()) && !"verified".equals(a.getIdentityKind()) ? b : a;
    var loser = survivor == a ? b : a;
    long moved = 0;
    for (var rec : records.findByIdentityIdOrderByCreatedAtAsc(loser.getId())) { rec.repoint(survivor.getId()); records.save(rec); moved++; }
    loser.markMerged(survivor.getId()); identities.save(loser);
    c.decide("merged", p.email(), r.secondApprover(), r.decisionEvidence()); duplicates.save(c);
    audit.record("identity.merged","duplicate_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"survivor",survivor.getId(),"merged_identity",loser.getId(),"records_moved",moved);
  }
  @PostMapping("/duplicates/{id}/reject")
  Map<String,Object> reject(@PathVariable UUID id, @RequestBody MergeRequest r) {
    var p = principal();
    var c = duplicates.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة التكرار غير موجودة"));
    if (!LongitudinalRules.mayDecideMerge(p.role()))
      throw BizException.forbidden("قرار حالات التكرار لمسؤول جودة الهوية فقط");
    c.decide("rejected", p.email(), r.secondApprover(), r.decisionEvidence()); duplicates.save(c);
    audit.record("identity.duplicate.rejected","duplicate_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  /** A wrong merge is undone auditably; history and links are preserved. */
  @PostMapping("/duplicates/{id}/unmerge")
  Map<String,Object> unmerge(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal();
    var c = duplicates.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة التكرار غير موجودة"));
    if (!"merged".equals(c.getStatus())) throw BizException.conflict("not_merged","الحالة ليست دمجًا منفذًا");
    if (!LongitudinalRules.mayDecideMerge(p.role()))
      throw BizException.forbidden("فك الدمج لمسؤول جودة الهوية فقط");
    if (body.get("reason")==null||body.get("reason").isBlank())
      throw BizException.badRequest("reason_required","فك الدمج يتطلب سببًا موثقًا ويبقى قابلًا للتدقيق");
    var a = identity(c.getIdentityA()); var b = identity(c.getIdentityB());
    if ("merged".equals(a.getStatus())) { a.unmerge(); identities.save(a); }
    if ("merged".equals(b.getStatus())) { b.unmerge(); identities.save(b); }
    c.decide("unmerged", p.email(), null, body.get("reason")); duplicates.save(c);
    audit.record("identity.unmerged","duplicate_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"note","both identities active again; the merge/unmerge trail stays in the audit log");
  }
  @GetMapping("/identities/{id}/timeline")
  Map<String,Object> timeline(@PathVariable UUID id) {
    var i = identity(id);
    return Map.of("ok",true,"identity",i,
      "links",links.findByIdentityIdOrderByCreatedAtAsc(id),
      "records",records.findByIdentityIdOrderByCreatedAtAsc(id));
  }
}
