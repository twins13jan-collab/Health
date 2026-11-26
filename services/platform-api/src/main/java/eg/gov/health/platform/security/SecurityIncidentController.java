package eg.gov.health.platform.security;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Security & privacy incidents (cycle 238). One register with timelines
 *  and owners; evidence is hashed with an append-only custody chain and
 *  is never modified; containment of a compromised account means its
 *  sessions are revoked; ransomware recovery means a VERIFIED clean
 *  backup; leak scope is unconfirmed until the privacy officer assesses
 *  it; notification decisions are justified either way; closure needs
 *  lessons learned with corrective actions closed. */
@RestController @RequestMapping("/api/security-incidents")
public class SecurityIncidentController {
  private final SecurityIncidentRepository incidents; private final EvidenceItemRepository evidence;
  private final IncidentActionRepository actions; private final PrivacyImpactRepository impacts;
  private final NotificationRecordRepository notifications; private final LessonsLearnedRepository lessons;
  private final AuditService audit;
  public SecurityIncidentController(SecurityIncidentRepository incidents, EvidenceItemRepository evidence,
                                    IncidentActionRepository actions, PrivacyImpactRepository impacts,
                                    NotificationRecordRepository notifications, LessonsLearnedRepository lessons, AuditService audit) {
    this.incidents=incidents; this.evidence=evidence; this.actions=actions; this.impacts=impacts;
    this.notifications=notifications; this.lessons=lessons; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private SecurityIncident incident(UUID id) {
    return incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
  }

  public record IncidentRequest(String type, String systems, String assets, String source, String impact, String severity) {}
  @PostMapping
  Map<String,Object> report(@RequestBody IncidentRequest r) {
    var p = principal();
    if (r.type()==null) throw BizException.badRequest("type_required","نوع الحادث مطلوب");
    var i = incidents.save(new SecurityIncident(UUID.fromString(p.facilityId()), p.governorate(), r.type(),
      r.systems(), r.assets(), r.source(), r.impact(), r.severity()));
    audit.record("security.incident.reported","security_incident",i.getId().toString(),"{}");
    return Map.of("ok",true,"incident",i,
      "note", "monitoring_gap".equals(r.type()) ? "a monitoring failure is a visibility gap — not evidence that nothing happened" : "");
  }
  @PostMapping("/{id}/revoke-sessions")
  Map<String,Object> revoke(@PathVariable UUID id) {
    var i = incident(id); i.revokeSessions(); incidents.save(i);
    audit.record("security.incident.sessions_revoked","security_incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",i);
  }
  @PostMapping("/{id}/verify-clean-backup")
  Map<String,Object> verifyBackup(@PathVariable UUID id) {
    var i = incident(id); i.verifyCleanBackup(); incidents.save(i);
    audit.record("security.incident.clean_backup_verified","security_incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",i,"note","recovery is proven in practice — a backup's existence alone proves nothing");
  }

  public record EvidenceRequest(String kind, String description, String sha256Hash, String collectedBy) {}
  @PostMapping("/{id}/evidence")
  Map<String,Object> evidence(@PathVariable UUID id, @RequestBody EvidenceRequest r) {
    var p = principal(); var i = incident(id);
    if (!SecurityIncidentRules.evidenceValid(r.sha256Hash()))
      throw BizException.badRequest("hash_required","الدليل بلا بصمة SHA-256 صالحة لا يُقبل — الأدلة ببصمات وسلسلة حيازة (دورة 238)");
    var e = evidence.save(new EvidenceItem(i.getFacilityId(), p.governorate(), id, r.kind(), r.description(), r.sha256Hash(), r.collectedBy()));
    audit.record("security.evidence.collected","evidence_item",e.getId().toString(),"{}");
    return Map.of("ok",true,"evidence",e,"immutable",true);
  }
  @PostMapping("/evidence/{id}/custody")
  Map<String,Object> custody(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var e = evidence.findById(id).orElseThrow(() -> BizException.notFound("evidence_not_found","الدليل غير موجود"));
    String holder = body.get("holder"), action = body.get("action");
    if (holder==null||action==null) throw BizException.badRequest("custody_detail","نقل الحيازة يتطلب الحائز والفعل");
    e.appendCustody("\"" + action + " -> " + holder + "\""); evidence.save(e);
    audit.record("security.evidence.custody","evidence_item",id.toString(),"{}");
    return Map.of("ok",true,"evidence",e);
  }

  public record ActionRequest(String phase, String action, String owner, Boolean cleanBackupVerified) {}
  @PostMapping("/{id}/actions")
  Map<String,Object> action(@PathVariable UUID id, @RequestBody ActionRequest r) {
    var p = principal(); var i = incident(id);
    if (r.phase()==null||r.action()==null) throw BizException.badRequest("invalid_action","المرحلة والإجراء مطلوبان");
    if ("containment".equals(r.phase()) && !SecurityIncidentRules.containmentValid(i.getType(), i.isSessionsRevoked()))
      throw BizException.conflict("revoke_first","الحساب المخترق لا يُحتوى قبل إبطال جلساته ورموزه");
    if ("recovery".equals(r.phase()) && !SecurityIncidentRules.recoveryValid(i.getType(),
        Boolean.TRUE.equals(r.cleanBackupVerified()) || i.isCleanBackupVerified()))
      throw BizException.conflict("clean_backup_first","هجوم الفدية: لا استعادة قبل التحقق من نسخ نظيفة");
    var a = actions.save(new IncidentAction(i.getFacilityId(), p.governorate(), id, r.phase(), r.action(), r.owner(),
      Boolean.TRUE.equals(r.cleanBackupVerified())));
    switch (r.phase()) {
      case "containment" -> i.setStatus("contained");
      case "eradication" -> i.setStatus("eradicated");
      case "recovery" -> i.setStatus("recovered");
      default -> { }
    }
    incidents.save(i);
    audit.record("security.action."+r.phase(),"incident_action",a.getId().toString(),"{}");
    return Map.of("ok",true,"action",a,"incident_status",i.getStatus());
  }

  public record PrivacyRequest(String dataTypes, Integer affectedPersonsCount, String disclosureLikelihood) {}
  @PostMapping("/{id}/privacy-impact")
  Map<String,Object> privacy(@PathVariable UUID id, @RequestBody PrivacyRequest r) {
    var p = principal(); var i = incident(id);
    // Only the privacy function assesses impact and notification.
    if (!SecurityIncidentRules.mayAssessPrivacy(p.role())) {
      audit.record("security.privacy.denied_role","security_incident",id.toString(),"{}");
      throw BizException.forbidden("تقييم أثر الخصوصية لمسؤول الخصوصية/الأمن فقط — سُجلت المحاولة");
    }
    var im = impacts.save(new PrivacyImpact(i.getFacilityId(), p.governorate(), id, r.dataTypes(),
      r.affectedPersonsCount(), r.disclosureLikelihood(), p.email()));
    audit.record("security.privacy.assessed","privacy_impact",im.getId().toString(),"{}");
    return Map.of("ok",true,"impact",im,
      "scope_status", r.affectedPersonsCount()==null ? "unconfirmed — no number is published until assessed" : "assessed");
  }

  public record NotificationRequest(String audience, String decision, String justification, String decidedBy) {}
  @PostMapping("/{id}/notifications")
  Map<String,Object> notify(@PathVariable UUID id, @RequestBody NotificationRequest r) {
    var p = principal(); var i = incident(id);
    if (r.audience()==null||r.decision()==null) throw BizException.badRequest("invalid_notification","الجمهور والقرار مطلوبان");
    if (!SecurityIncidentRules.notificationDecisionValid(r.justification()))
      throw BizException.badRequest("justification_required","قرار الإخطار — بالإخطار أو عدمه — يوثق بمبرراته دائمًا (دورة 238)");
    var n = notifications.save(new NotificationRecord(i.getFacilityId(), p.governorate(), id, r.audience(), r.decision(), r.justification(), r.decidedBy()));
    audit.record("security.notification.decided","notification_record",n.getId().toString(),"{}");
    return Map.of("ok",true,"notification",n);
  }

  public record LessonsRequest(String lessons, String correctiveActions) {}
  @PostMapping("/{id}/lessons")
  Map<String,Object> lessons(@PathVariable UUID id, @RequestBody LessonsRequest r) {
    var p = principal(); var i = incident(id);
    if (r.lessons()==null||r.lessons().isBlank()) throw BizException.badRequest("lessons_required","مراجعة ما بعد الحادث تتطلب دروسًا موثقة");
    var l = lessons.save(new LessonsLearned(i.getFacilityId(), p.governorate(), id, r.lessons(), r.correctiveActions()));
    audit.record("security.lessons.recorded","lessons_learned",l.getId().toString(),"{}");
    return Map.of("ok",true,"lessons",l);
  }
  @PostMapping("/lessons/{id}/close-corrective")
  Map<String,Object> closeCorrective(@PathVariable UUID id) {
    var l = lessons.findById(id).orElseThrow(() -> BizException.notFound("lessons_not_found","سجل الدروس غير موجود"));
    l.closeCorrective(); lessons.save(l);
    audit.record("security.lessons.corrective_closed","lessons_learned",id.toString(),"{}");
    return Map.of("ok",true,"lessons",l);
  }
  @PostMapping("/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    var i = incident(id);
    boolean hasContainment = actions.findByIncidentIdOrderByCreatedAtAsc(id).stream().anyMatch(a -> "containment".equals(a.getPhase()));
    var latestLessons = lessons.findByIncidentIdOrderByCreatedAtDesc(id).stream().findFirst().orElse(null);
    boolean closable = SecurityIncidentRules.closable(hasContainment, "recovered".equals(i.getStatus()),
      latestLessons!=null, latestLessons!=null && latestLessons.isCorrectiveClosed());
    if (!closable)
      throw BizException.conflict("not_closable","الإغلاق يتطلب احتواءً واستعادة موثقة ودروسًا وإجراءات تصحيحية مغلقة");
    i.setStatus("closed"); incidents.save(i);
    audit.record("security.incident.closed","security_incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",i);
  }

  @GetMapping
  Map<String,Object> list() {
    var rows = incidents.findAll();
    return Map.of("ok",true,"incidents",rows,"count",rows.size());
  }
}
