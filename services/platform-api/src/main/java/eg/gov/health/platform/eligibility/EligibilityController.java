package eg.gov.health.platform.eligibility;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Service eligibility verification BEFORE booking (module 183).
 *  The check verifies the service really exists at the requested
 *  slot — offered, licensed, with capacity, staff, equipment and
 *  consumables — plus preparation requirements and clinical
 *  eligibility. Coverage and pre-authorization are verified and
 *  reported, but they never enter the decision: a financial status
 *  cannot make a clinically eligible patient ineligible. Every
 *  failed check is named in the restrictions, and the check records
 *  its time, its checker and its source. */
@RestController @RequestMapping("/api/eligibility")
public class EligibilityController {
  private final ServiceAvailabilityCheckRepository checks; private final AuditService audit;
  public EligibilityController(ServiceAvailabilityCheckRepository checks, AuditService audit) {
    this.checks=checks; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record CheckRequest(String patientRef, String serviceCode, String serviceName, OffsetDateTime slotAt,
                             Boolean serviceOffered, Boolean licenseValid, Boolean capacityAvailable, Boolean staffAvailable,
                             Boolean equipmentAvailable, Boolean consumablesAvailable, Boolean preparationMet, Boolean clinicalEligible,
                             String coverageStatus, String source) {}
  @PostMapping("/checks")
  Map<String,Object> check(@RequestBody CheckRequest r) {
    var p = principal();
    var failed = EligibilityRules.restrictions(Boolean.TRUE.equals(r.serviceOffered()), Boolean.TRUE.equals(r.licenseValid()),
      Boolean.TRUE.equals(r.capacityAvailable()), Boolean.TRUE.equals(r.staffAvailable()), Boolean.TRUE.equals(r.equipmentAvailable()),
      Boolean.TRUE.equals(r.consumablesAvailable()), Boolean.TRUE.equals(r.clinicalEligible()));
    String result = EligibilityRules.decide(failed, Boolean.TRUE.equals(r.preparationMet()));
    var c = checks.save(new ServiceAvailabilityCheck(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.serviceCode(),
      r.serviceName(), r.slotAt(), Boolean.TRUE.equals(r.serviceOffered()), Boolean.TRUE.equals(r.licenseValid()),
      Boolean.TRUE.equals(r.capacityAvailable()), Boolean.TRUE.equals(r.staffAvailable()), Boolean.TRUE.equals(r.equipmentAvailable()),
      Boolean.TRUE.equals(r.consumablesAvailable()), Boolean.TRUE.equals(r.preparationMet()), Boolean.TRUE.equals(r.clinicalEligible()),
      r.coverageStatus(), result, String.join(",", failed), p.email(), r.source()));
    audit.record("eligibility.checked","service_availability_check",c.getId().toString(),"{}");
    return Map.of("ok",true,"check",c,"result",result,"restrictions",failed,
      "financial_note",EligibilityRules.financialNote(r.coverageStatus()),
      "decision_basis","operational and clinical checks only — coverage is reported, never decisive");
  }
}
