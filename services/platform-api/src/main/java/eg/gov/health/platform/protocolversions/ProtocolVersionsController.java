package eg.gov.health.platform.protocolversions;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Protocol, policy & clinical-rule versions (module 61).
 *  Treatment protocols by specialty, condition, age group and
 *  use determinants; the scientific reference, owning body,
 *  approving committee and issue, effective and review dates
 *  recorded; amendments, comments, approvals, exceptions and
 *  withdrawal of the old version governed; every session,
 *  procedure, dose or clinical decision linked to the protocol
 *  version used; dosing, interaction and early-warning rules
 *  held as versioned governed content, not undocumented logic;
 *  and a register showing which protocol was in force when a
 *  service was delivered. */
@RestController @RequestMapping("/api/protocol-versions")
public class ProtocolVersionsController {
  private final ClinicalProtocolVersionRepository versions; private final ClinicalProtocolUsageRepository usages; private final AuditService audit;
  public ProtocolVersionsController(ClinicalProtocolVersionRepository versions, ClinicalProtocolUsageRepository usages, AuditService audit) {
    this.versions=versions; this.usages=usages; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ClinicalProtocolVersion version(UUID id) {
    return versions.findById(id).orElseThrow(() -> BizException.notFound("protocol_not_found","البروتوكول غير موجود"));
  }

  public record VersionBody(String code, String title, String version, String scientificRef, String ownerBody,
                            Boolean committeeApproved, LocalDate effectiveFrom, LocalDate reviewDue) {}
  @PostMapping("/versions")
  Map<String,Object> create(@RequestBody VersionBody r) {
    var p = principal();
    var v = versions.save(new ClinicalProtocolVersion(UUID.fromString(p.facilityId()), p.governorate(), r.code(), r.title(), r.version(),
      r.scientificRef(), r.ownerBody(), Boolean.TRUE.equals(r.committeeApproved()), r.effectiveFrom(), r.reviewDue()));
    return Map.of("ok",true,"protocol",v);
  }
  @PostMapping("/versions/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var v = version(id);
    var blockers = ProtocolRules.activationBlockers(v.getScientificRef(), v.getOwnerBody(), v.isCommitteeApproved(), v.getEffectiveFrom());
    if (!blockers.isEmpty())
      throw BizException.conflict("not_activatable","إصدار بروتوكول يسري فقط مكتملًا: " + String.join("، ", blockers) + " — قواعد جرعات وتداخلات محتوى محكوم الإصدار لا منطق غير موثق");
    v.activate(); versions.save(v);
    return Map.of("ok",true,"protocol",v);
  }
  @PostMapping("/versions/{id}/withdraw")
  Map<String,Object> withdraw(@PathVariable UUID id) {
    principal(); var v = version(id);
    v.withdraw(); versions.save(v);
    audit.record("protocol.withdrawn","clinical_protocol_version",id.toString(),"{}");
    return Map.of("ok",true,"protocol",v);
  }
  public record UsageBody(String serviceRef, LocalDate serviceDate) {}
  @PostMapping("/versions/{id}/usages")
  Map<String,Object> usage(@PathVariable UUID id, @RequestBody UsageBody r) {
    var p = principal(); var v = version(id);
    if (!ProtocolRules.inForce(v.getStatus(), v.getEffectiveFrom(), r.serviceDate()))
      throw BizException.conflict("not_in_force","خدمة تُربط فقط بالإصدار الساري وقت تقديمها — إصدار مسحوب أو لم يبدأ سريانه لا يحكم خدمة جديدة");
    var u = usages.save(new ClinicalProtocolUsage(UUID.fromString(p.facilityId()), p.governorate(), id, r.serviceRef(), r.serviceDate()));
    return Map.of("ok",true,"usage",u,"protocol_version",v.getVersion());
  }
  @GetMapping("/in-force")
  Map<String,Object> inForce(@RequestParam String code, @RequestParam LocalDate onDate) {
    principal();
    var governing = versions.findByCodeOrderByEffectiveFromDesc(code).stream()
      .filter(v -> ProtocolRules.inForce(v.getStatus(), v.getEffectiveFrom(), onDate)).findFirst().orElse(null);
    return Map.of("ok",true,"code",code,"on_date",onDate.toString(),
      "governing_version",governing == null ? "" : governing.getVersion(),
      "found",governing != null);
  }
}
