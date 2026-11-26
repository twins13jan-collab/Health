package eg.gov.health.platform.accreditation;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Accreditation, licensing & inspection (module 131).
 *  Licence files for facility, department, service and practice;
 *  standards, evidence and expiry dates; inspections with
 *  findings and corrective actions; suspension or restriction
 *  of a service by a competent authority's decision; and
 *  verification of provider licences. */
@RestController @RequestMapping("/api/accreditation")
public class AccreditationController {
  private final LicenceFileRepository licences; private final LicenceInspectionRepository inspections;
  private final ServiceRestrictionRepository restrictions; private final AuditService audit;
  public AccreditationController(LicenceFileRepository licences, LicenceInspectionRepository inspections,
                                 ServiceRestrictionRepository restrictions, AuditService audit) {
    this.licences=licences; this.inspections=inspections; this.restrictions=restrictions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private LicenceFile licence(UUID id) {
    return licences.findById(id).orElseThrow(() -> BizException.notFound("licence_not_found","الترخيص غير موجود"));
  }

  public record LicenceBody(String scope, String holderRef, String standardRef, LocalDate issuedAt, LocalDate expiresAt) {}
  @PostMapping("/licences")
  Map<String,Object> create(@RequestBody LicenceBody r) {
    var p = principal();
    var l = licences.save(new LicenceFile(UUID.fromString(p.facilityId()), p.governorate(), r.scope(), r.holderRef(), r.standardRef(), r.issuedAt(), r.expiresAt()));
    return Map.of("ok",true,"licence",l);
  }
  @GetMapping("/licences/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id) {
    principal(); var l = licence(id);
    boolean valid = AccreditationRules.licenceValid(l.getStatus(), l.getExpiresAt(), LocalDate.now());
    return Map.of("ok",true,"licence_id",id.toString(),"status",l.getStatus(),
      "expires_at",l.getExpiresAt() == null ? "" : l.getExpiresAt().toString(),"valid",valid,
      "rule","an expired or suspended licence means the service is unlicensed — whatever the framed certificate says");
  }
  public record InspectionBody(String inspector, String findings, String correctiveActions) {}
  @PostMapping("/licences/{id}/inspections")
  Map<String,Object> inspect(@PathVariable UUID id, @RequestBody InspectionBody r) {
    var p = principal(); licence(id);
    var i = inspections.save(new LicenceInspection(UUID.fromString(p.facilityId()), p.governorate(), id, r.inspector(), r.findings(), r.correctiveActions()));
    audit.record("accreditation.inspected","licence_inspection",i.getId().toString(),"{}");
    return Map.of("ok",true,"inspection",i);
  }
  @PostMapping("/inspections/{iid}/corrective-done")
  Map<String,Object> corrective(@PathVariable UUID iid) {
    principal();
    var i = inspections.findById(iid).orElseThrow(() -> BizException.notFound("inspection_not_found","التفتيش غير موجود"));
    i.completeCorrective(); inspections.save(i);
    return Map.of("ok",true,"inspection",i);
  }
  @PostMapping("/inspections/{iid}/close")
  Map<String,Object> closeInspection(@PathVariable UUID iid) {
    principal();
    var i = inspections.findById(iid).orElseThrow(() -> BizException.notFound("inspection_not_found","التفتيش غير موجود"));
    if (!AccreditationRules.inspectionClosable(i.getFindings(), i.isCorrectiveDone()))
      throw BizException.conflict("corrective_open","تفتيش بملاحظات لا يُغلق قبل إتمام الإجراءات التصحيحية");
    i.close(); inspections.save(i);
    return Map.of("ok",true,"inspection",i);
  }
  public record RestrictionBody(String restrictionType, String decidedBy, String authority, String reason) {}
  @PostMapping("/licences/{id}/restrict")
  Map<String,Object> restrict(@PathVariable UUID id, @RequestBody RestrictionBody r) {
    var p = principal(); var l = licence(id);
    if (r.decidedBy() == null || r.decidedBy().isBlank() || r.authority() == null || r.authority().isBlank())
      throw BizException.badRequest("authority_required","تعليق أو تقييد خدمة قرار جهة مختصة مسماة بسندها — لا مفتاح مجهول");
    var sr = restrictions.save(new ServiceRestriction(UUID.fromString(p.facilityId()), p.governorate(), id, r.restrictionType(), r.decidedBy(), r.authority(), r.reason()));
    l.setStatus("suspend".equals(r.restrictionType()) ? "suspended" : "restricted"); licences.save(l);
    audit.record("accreditation.service."+r.restrictionType(),"service_restriction",sr.getId().toString(),"{}");
    return Map.of("ok",true,"restriction",sr,"licence_status",l.getStatus());
  }
}
