package eg.gov.health.platform.continuity;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Business continuity & technical recovery (module 123).
 *  Service-outage impact is analysed; backup, restore and
 *  replication are managed against explicit recovery-time and
 *  recovery-point objectives; ACTUAL restore tests are run and
 *  documented; and alternate operations take over when the
 *  network or the data centre is lost. */
@RestController @RequestMapping("/api/continuity")
public class ContinuityController {
  private final CriticalServiceRepository services; private final RestoreTestRepository tests;
  private final FailoverEventRepository failovers; private final AuditService audit;
  public ContinuityController(CriticalServiceRepository services, RestoreTestRepository tests, FailoverEventRepository failovers, AuditService audit) {
    this.services=services; this.tests=tests; this.failovers=failovers; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CriticalService service(UUID id) {
    return services.findById(id).orElseThrow(() -> BizException.notFound("service_not_found","الخدمة غير موجودة"));
  }

  public record ServiceBody(String name, String impactLevel, Integer rtoMinutes, Integer rpoMinutes, Boolean backupConfigured) {}
  @PostMapping("/services")
  Map<String,Object> create(@RequestBody ServiceBody r) {
    var p = principal();
    var s = services.save(new CriticalService(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.impactLevel(),
      r.rtoMinutes(), r.rpoMinutes(), Boolean.TRUE.equals(r.backupConfigured())));
    return Map.of("ok",true,"service",s,"recovery_proven",false,"note","a configured backup is a hope — only a passed restore test is proof");
  }
  public record TestBody(LocalDate testedAt, Integer actualRtoMinutes, Integer actualRpoMinutes, Boolean success) {}
  @PostMapping("/services/{id}/restore-tests")
  Map<String,Object> restoreTest(@PathVariable UUID id, @RequestBody TestBody r) {
    var p = principal(); var s = service(id);
    var t = tests.save(new RestoreTest(UUID.fromString(p.facilityId()), p.governorate(), id, r.testedAt(),
      r.actualRtoMinutes(), r.actualRpoMinutes(), Boolean.TRUE.equals(r.success())));
    boolean passed = ContinuityRules.restoreTestPassed(r.actualRtoMinutes(), r.actualRpoMinutes(), s.getRtoMinutes(), s.getRpoMinutes(), Boolean.TRUE.equals(r.success()));
    audit.record("continuity.restore.tested","restore_test",t.getId().toString(),"{}");
    return Map.of("ok",true,"test",t,"targets_met",passed);
  }
  @GetMapping("/services/{id}/readiness")
  Map<String,Object> readiness(@PathVariable UUID id) {
    principal(); var s = service(id);
    LocalDate lastPassed = null;
    for (var t : tests.findByServiceIdOrderByTestedAtDesc(id)) {
      if (ContinuityRules.restoreTestPassed(t.getActualRtoMinutes(), t.getActualRpoMinutes(), s.getRtoMinutes(), s.getRpoMinutes(), t.isSuccess())) {
        lastPassed = t.getTestedAt(); break;
      }
    }
    boolean proven = ContinuityRules.recoveryProven(lastPassed, LocalDate.now());
    return Map.of("ok",true,"service_id",id.toString(),"last_passed_restore_test",lastPassed == null ? "" : lastPassed.toString(),
      "recovery_proven",proven,"proof_rule","a passed restore test within 6 months — backups alone prove nothing");
  }
  public record FailoverBody(String mode, String reason) {}
  @PostMapping("/services/{id}/failover")
  Map<String,Object> failover(@PathVariable UUID id, @RequestBody FailoverBody r) {
    var p = principal(); service(id);
    if (r.reason() == null || r.reason().isBlank())
      throw BizException.badRequest("reason_required","التحول للتشغيل البديل فعل معلن بسبب موثق — لا انجراف صامت");
    var f = failovers.save(new FailoverEvent(UUID.fromString(p.facilityId()), p.governorate(), id, r.mode(), r.reason()));
    audit.record("continuity.failover."+r.mode(),"failover_event",f.getId().toString(),"{}");
    return Map.of("ok",true,"failover",f);
  }
}
