package eg.gov.health.platform.clinicalcomms;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Clinical communication & critical-information handover
 *  (module 161). Critical notifications between physician,
 *  nursing, laboratory, radiology and pharmacy run closed-loop:
 *  receipt is acknowledged and the content read back before the
 *  message counts as communicated. Shift handovers and transfers
 *  are managed, open critical tasks alert their owners, and
 *  fallback channels are used — and recorded — when the system
 *  channel is down. */
@RestController @RequestMapping("/api/clinical-comms")
public class ClinicalCommsController {
  private final CriticalAlertRepository alerts; private final AlertEscalationRepository escalations; private final AuditService audit;
  public ClinicalCommsController(CriticalAlertRepository alerts, AlertEscalationRepository escalations, AuditService audit) {
    this.alerts=alerts; this.escalations=escalations; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CriticalAlert alert(UUID id) {
    return alerts.findById(id).orElseThrow(() -> BizException.notFound("alert_not_found","البلاغ غير موجود"));
  }

  public record AlertRequest(String patientRef, String toRole, String messageType, String content, String channel, Boolean fallbackUsed) {}
  @PostMapping("/alerts")
  Map<String,Object> send(@RequestBody AlertRequest r) {
    var p = principal();
    var a = alerts.save(new CriticalAlert(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), p.role(), r.toRole(),
      r.messageType(), r.content(), r.channel(), Boolean.TRUE.equals(r.fallbackUsed())));
    audit.record("comms.alert.sent","critical_alert",a.getId().toString(),"{}");
    return Map.of("ok",true,"alert",a,"closed_loop","not communicated until acknowledged AND read back");
  }
  @PostMapping("/alerts/{id}/acknowledge")
  Map<String,Object> ack(@PathVariable UUID id) {
    principal(); var a = alert(id);
    a.acknowledge(); alerts.save(a);
    return Map.of("ok",true,"alert",a);
  }
  @PostMapping("/alerts/{id}/read-back")
  Map<String,Object> readBack(@PathVariable UUID id) {
    principal(); var a = alert(id);
    if (a.getAcknowledgedAt() == null) throw BizException.conflict("ack_first","إعادة القراءة بعد تأكيد الاستلام");
    a.readBack(); alerts.save(a);
    audit.record("comms.alert.read_back","critical_alert",id.toString(),"{}");
    return Map.of("ok",true,"alert",a,"loop_closed",CommsRules.loopClosed(true, a.isReadBackConfirmed()));
  }
  @PostMapping("/alerts/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var a = alert(id);
    if (!CommsRules.loopClosed(a.getAcknowledgedAt() != null, a.isReadBackConfirmed()))
      throw BizException.conflict("loop_open","البلاغ الحرج لا يُغلق قبل تأكيد الاستلام وإعادة القراءة — «أُرسل» أمل لا تسليم");
    a.close(); alerts.save(a);
    return Map.of("ok",true,"alert",a);
  }
  @GetMapping("/alerts/{id}/escalation-check")
  Map<String,Object> escalationCheck(@PathVariable UUID id) {
    principal(); var a = alert(id);
    boolean due = CommsRules.escalationDue(a.getSentAt(), a.getAcknowledgedAt() != null, OffsetDateTime.now());
    return Map.of("ok",true,"alert_id",id.toString(),"escalation_due",due,
      "threshold_minutes",CommsRules.ACK_ESCALATION_MINUTES);
  }
  public record EscalateRequest(String escalatedTo, String reason) {}
  @PostMapping("/alerts/{id}/escalate")
  Map<String,Object> escalate(@PathVariable UUID id, @RequestBody EscalateRequest r) {
    var p = principal(); alert(id);
    var e = escalations.save(new AlertEscalation(UUID.fromString(p.facilityId()), p.governorate(), id, r.escalatedTo(), r.reason()));
    audit.record("comms.alert.escalated","alert_escalation",e.getId().toString(),"{}");
    return Map.of("ok",true,"escalation",e);
  }
}
