package eg.gov.health.platform.capabilityaccess;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Service accreditation & availability by capability
 *  (module 160). A service is linked to its staff, equipment,
 *  licences and protocols; activation and temporary-suspension
 *  conditions governed; continuing capability verified
 *  periodically; referral levels and centres of excellence
 *  managed; a service never announced available merely because
 *  its name exists; and the reason for unavailability and the
 *  last verification shown. */
@RestController @RequestMapping("/api/capability-access")
public class CapabilityAccessController {
  private final ServiceCapabilityRepository capabilities; private final AuditService audit;
  public CapabilityAccessController(ServiceCapabilityRepository capabilities, AuditService audit) {
    this.capabilities=capabilities; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record CapBody(String serviceRef, Boolean staffReady, Boolean equipmentReady, Boolean licenceValid,
                        Boolean protocolLinked, OffsetDateTime lastVerifiedAt) {}
  @PostMapping("/capabilities")
  Map<String,Object> create(@RequestBody CapBody r) {
    var p = principal();
    var c = capabilities.save(new ServiceCapability(UUID.fromString(p.facilityId()), p.governorate(), r.serviceRef(),
      Boolean.TRUE.equals(r.staffReady()), Boolean.TRUE.equals(r.equipmentReady()), Boolean.TRUE.equals(r.licenceValid()),
      Boolean.TRUE.equals(r.protocolLinked()), r.lastVerifiedAt()));
    return Map.of("ok",true,"capability",c);
  }
  @GetMapping("/capabilities/{id}/availability")
  Map<String,Object> availability(@PathVariable UUID id) {
    principal();
    var c = capabilities.findById(id).orElseThrow(() -> BizException.notFound("capability_not_found","القدرة غير موجودة"));
    var now = OffsetDateTime.now();
    var missing = CapabilityRules.missingPillars(c.isStaffReady(), c.isEquipmentReady(), c.isLicenceValid(), c.isProtocolLinked());
    boolean fresh = CapabilityRules.fresh(c.getLastVerifiedAt(), now);
    boolean available = missing.isEmpty() && fresh;
    List<String> reasons = new ArrayList<>(missing);
    if (!fresh) reasons.add("verification_stale");
    return Map.of("ok",true,"capability",c,"available",available,
      "unavailable_reasons",reasons,"last_verified_at",c.getLastVerifiedAt() == null ? "" : c.getLastVerifiedAt().toString(),
      "note","a service name in a directory is not availability — the capability must be real and freshly verified");
  }
}
