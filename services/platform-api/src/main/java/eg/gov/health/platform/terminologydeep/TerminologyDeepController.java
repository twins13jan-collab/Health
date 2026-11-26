package eg.gov.health.platform.terminologydeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Unified event & terminology definitions (module 137). A
 *  dictionary of clinical, administrative and financial
 *  events; local terms mapped to the appropriate standards;
 *  dictionary versions and effectivity governed; meaning
 *  preservation in conversion verified; terms reviewed by
 *  specialists; and unsupported terms documented. */
@RestController @RequestMapping("/api/terminology-deep")
public class TerminologyDeepController {
  private final TermMappingRepository mappings; private final AuditService audit;
  public TerminologyDeepController(TermMappingRepository mappings, AuditService audit) {
    this.mappings=mappings; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record MappingBody(String localTerm, String standardSystem, String standardCode, Boolean meaningVerified,
                            Boolean specialistReviewed, Boolean supported) {}
  @PostMapping("/mappings")
  Map<String,Object> create(@RequestBody MappingBody r) {
    var p = principal();
    boolean supported = !Boolean.FALSE.equals(r.supported());
    var m = mappings.save(new TermMapping(UUID.fromString(p.facilityId()), p.governorate(), r.localTerm(), r.standardSystem(), r.standardCode(),
      Boolean.TRUE.equals(r.meaningVerified()), Boolean.TRUE.equals(r.specialistReviewed()), supported));
    return Map.of("ok",true,"mapping",m,
      "usable_for_conversion",TerminologyRules.usable(m.isSupported(), m.isMeaningVerified(), m.isSpecialistReviewed()),
      "note", supported ? "a mapping converts data only when meaning preservation is verified and a specialist reviewed it"
                        : "documented as unsupported — never force-mapped to the nearest code");
  }
  @GetMapping("/mappings/{id}/usable")
  Map<String,Object> usable(@PathVariable UUID id) {
    principal();
    var m = mappings.findById(id).orElseThrow(() -> BizException.notFound("mapping_not_found","الربط غير موجود"));
    return Map.of("ok",true,"mapping",m,"usable_for_conversion",
      TerminologyRules.usable(m.isSupported(), m.isMeaningVerified(), m.isSpecialistReviewed()));
  }
}
