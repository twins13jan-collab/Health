package eg.gov.health.platform.recordsdocs;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Documents, signatures, retention & legal holds (cycle 239). Every
 *  document carries an owner, classification, version, fingerprint
 *  and source. The creator versions but never deletes the approved
 *  document; a signature is an approval only when its certificate
 *  verifies within the signer's scope for the type; editing after
 *  signing mints a new version and supersedes the old signature;
 *  legally held or retention-bound records cannot be deleted —
 *  deletion requests against them are held with the basis documented;
 *  corrupted files keep their original under a restoration request;
 *  the records manager governs retention, not medical content; and
 *  view/print/download/sensitive-share are all access-logged. */
@RestController @RequestMapping("/api/records-documents")
public class RecordsDocsController {
  private final DocumentRecordRepository docs; private final DocumentVersionRepository versions;
  private final SignatureRecordRepository signatures; private final RetentionRuleRepository retention;
  private final LegalHoldRepository holds; private final DocumentAccessEventRepository access;
  private final DeletionRequestRepository deletions; private final AuditService audit;
  public RecordsDocsController(DocumentRecordRepository docs, DocumentVersionRepository versions, SignatureRecordRepository signatures,
                               RetentionRuleRepository retention, LegalHoldRepository holds, DocumentAccessEventRepository access,
                               DeletionRequestRepository deletions, AuditService audit) {
    this.docs=docs; this.versions=versions; this.signatures=signatures; this.retention=retention;
    this.holds=holds; this.access=access; this.deletions=deletions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DocumentRecord doc(UUID id) {
    return docs.findById(id).orElseThrow(() -> BizException.notFound("document_not_found","الوثيقة غير موجودة"));
  }
  private String currentHash(DocumentRecord d) {
    return versions.findByDocumentIdOrderByVersionAsc(d.getId()).stream()
      .filter(v -> v.getVersion()==d.getCurrentVersion()).map(DocumentVersion::getContentHash).findFirst().orElse(null);
  }

  public record DocRequest(String docType, String title, String classification, String confidentiality, String source, String contentRef, String contentHash) {}
  @PostMapping
  Map<String,Object> create(@RequestBody DocRequest r) {
    var p = principal();
    if (r.docType()==null||r.contentHash()==null)
      throw BizException.badRequest("doc_fields","كل وثيقة لها مالك وتصنيف وإصدار وبصمة ومصدر");
    var d = docs.save(new DocumentRecord(UUID.fromString(p.facilityId()), p.governorate(), r.docType(), r.title(),
      p.email(), r.classification(), r.confidentiality(), r.source()));
    versions.save(new DocumentVersion(d.getFacilityId(), p.governorate(), d.getId(), 1, r.contentRef(), r.contentHash(), p.email(), "initial version"));
    audit.record("document.created","document_record",d.getId().toString(),"{}");
    return Map.of("ok",true,"document",d,"version",1,"fingerprint",r.contentHash());
  }
  public record VersionRequest(String contentRef, String contentHash, String changeReason) {}
  @PostMapping("/{id}/versions")
  Map<String,Object> newVersion(@PathVariable UUID id, @RequestBody VersionRequest r) {
    var p = principal(); var d = doc(id);
    if (r.contentHash()==null||r.changeReason()==null)
      throw BizException.badRequest("version_fields","التعديل يتطلب بصمة جديدة وسبب تعديل — ولا يعدل المعتمد في مكانه");
    // Signatures on earlier versions are superseded by the edit.
    signatures.findByDocumentIdOrderByCreatedAtDesc(id).stream().filter(s -> "valid".equals(s.getStatus()))
      .forEach(s -> { s.supersede(); signatures.save(s); });
    d.bumpVersion(); docs.save(d);
    var v = versions.save(new DocumentVersion(d.getFacilityId(), p.governorate(), id, d.getCurrentVersion(), r.contentRef(), r.contentHash(), p.email(), r.changeReason()));
    audit.record("document.version.created","document_version",v.getId().toString(),"{}");
    return Map.of("ok",true,"version",v,"note","edit after signing created a new version; the previous signature does not carry over");
  }
  public record SignRequest(String certificateRef, Boolean certificateValid, String scope) {}
  @PostMapping("/{id}/sign")
  Map<String,Object> sign(@PathVariable UUID id, @RequestBody SignRequest r) {
    var p = principal(); var d = doc(id);
    String hash = currentHash(d);
    boolean inScope = r.scope()!=null && r.scope().equals(d.getDocType());
    var s = new SignatureRecord(d.getFacilityId(), p.governorate(), id, d.getCurrentVersion(), hash,
      p.email(), r.certificateRef(), Boolean.TRUE.equals(r.certificateValid()), r.scope());
    boolean counts = DocumentRules.signatureCounts(Boolean.TRUE.equals(r.certificateValid()), inScope);
    s.validate(counts); signatures.save(s);
    if (counts) { d.approve(d.getCurrentVersion()); docs.save(d); }
    audit.record("document.signed","signature_record",s.getId().toString(),"{}");
    return Map.of("ok",true,"signature",s,"counts_as_approval",counts,
      "note", counts ? "verified signature within scope approves this version"
                     : "an invalid or out-of-scope signature is NOT an approval");
  }
  @GetMapping("/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id) {
    var d = doc(id);
    String hash = currentHash(d);
    var sigs = signatures.findByDocumentIdOrderByCreatedAtDesc(id);
    boolean approvedValid = sigs.stream().anyMatch(s -> "valid".equals(s.getStatus())
        && DocumentRules.signatureAppliesTo(s.getVersionHash(), hash));
    return Map.of("ok",true,"document",d,"current_version_hash",String.valueOf(hash),
      "signatures",sigs,"approved_version_signature_valid",approvedValid);
  }
  public record AccessRequest(String action, String purpose, Boolean sensitive) {}
  @PostMapping("/{id}/access")
  Map<String,Object> access(@PathVariable UUID id, @RequestBody AccessRequest r) {
    var p = principal(); var d = doc(id);
    if (!Set.of("view","download","print","share").contains(r.action())||r.purpose()==null)
      throw BizException.badRequest("access_fields","تسجيل العرض والطباعة والتنزيل والمشاركة الحساسة يتطلب الفعل والغرض");
    var e = access.save(new DocumentAccessEvent(d.getFacilityId(), p.governorate(), id, p.email(), r.action(), r.purpose(), Boolean.TRUE.equals(r.sensitive())));
    audit.record("document.access."+r.action(),"document_access_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"event",e);
  }
  @PostMapping("/{id}/corruption")
  Map<String,Object> corruption(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var d = doc(id);
    String ref = "RESTORE-" + id.toString().substring(0, 8);
    d.markCorrupted(ref); docs.save(d);
    audit.record("document.corruption.reported","document_record",id.toString(),"{}");
    return Map.of("ok",true,"document",d,"restoration_request",ref,"note","the corrupted original is preserved; a restoration request is open");
  }
  public record RetentionRequest(String docType, Integer retentionYears, String legalBasis) {}
  @PostMapping("/retention-rules")
  Map<String,Object> retentionRule(@RequestBody RetentionRequest r) {
    var p = principal();
    if (!DocumentRules.mayManageRetention(p.role()))
      throw BizException.forbidden("مسؤول السجلات يدير الاحتفاظ — لا المحتوى الطبي ولا غير المختصين");
    var rule = retention.save(new RetentionRule(UUID.fromString(p.facilityId()), p.governorate(), r.docType(),
      r.retentionYears()==null?0:r.retentionYears(), r.legalBasis(), p.email()));
    audit.record("document.retention.defined","retention_rule",rule.getId().toString(),"{}");
    return Map.of("ok",true,"rule",rule);
  }
  @PostMapping("/{id}/legal-holds")
  Map<String,Object> hold(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var d = doc(id);
    var h = holds.save(new LegalHold(d.getFacilityId(), p.governorate(), id, body.get("caseRef"), body.get("reason"), p.email()));
    audit.record("document.legal_hold.placed","legal_hold",h.getId().toString(),"{}");
    return Map.of("ok",true,"hold",h);
  }
  @PostMapping("/legal-holds/{id}/release")
  Map<String,Object> releaseHold(@PathVariable UUID id) {
    var p = principal();
    var h = holds.findById(id).orElseThrow(() -> BizException.notFound("hold_not_found","أمر الحفظ غير موجود"));
    h.release(p.email()); holds.save(h);
    audit.record("document.legal_hold.released","legal_hold",id.toString(),"{}");
    return Map.of("ok",true,"hold",h);
  }
  /** Deletion is never executed against a held or retention-bound record. */
  @PostMapping("/{id}/deletion-requests")
  Map<String,Object> deletion(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var d = doc(id);
    boolean held = !holds.findByDocumentIdAndActiveTrue(id).isEmpty();
    LocalDate retentionUntil = null;
    var rule = retention.findByFacilityIdAndDocType(d.getFacilityId(), d.getDocType());
    if (rule.isPresent()) retentionUntil = d.getCreatedAt().toLocalDate().plusYears(rule.get().getRetentionYears());
    boolean blocked = DocumentRules.deletionBlocked(held, retentionUntil, LocalDate.now());
    String reason = held ? "active legal hold " : (blocked ? "retention obligation until " + retentionUntil : null);
    var req = deletions.save(new DeletionRequest(d.getFacilityId(), p.governorate(), id, p.email(), body.get("basis"),
      reason, blocked ? "held" : "approved"));
    audit.record("document.deletion.requested","deletion_request",req.getId().toString(),"{}");
    return Map.of("ok",true,"deletion_request",req,"executed",false,
      "note", blocked ? "deletion held with documented basis — legally protected records are never deleted"
                      : "approved for the records workflow; execution stays a separate audited step");
  }
  @GetMapping("/{id}")
  Map<String,Object> get(@PathVariable UUID id) {
    var d = doc(id);
    return Map.of("ok",true,"document",d,"versions",versions.findByDocumentIdOrderByVersionAsc(id),
      "access_events",access.findByDocumentIdOrderByCreatedAtAsc(id));
  }
}
