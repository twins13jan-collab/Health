package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/research")
public class ResearchController {
  private final ResearchRequestRepository repo; private final AuditService audit;
  public ResearchController(ResearchRequestRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ResearchReq(String purpose, List<String> fields) {}
  @PostMapping("/requests")
  Map<String,Object> submit(@RequestBody ResearchReq r) {
    var p = TenantContext.get();
    if (r.purpose()==null||r.fields()==null||r.fields().isEmpty()) throw BizException.badRequest("missing_field","purpose و fields مطلوبة");
    var violations = ResearchExtractRules.violations(r.fields());
    if (!violations.isEmpty())
      throw BizException.badRequest("direct_identifiers_forbidden","حقول تعريف مباشر ممنوعة في أي مستخرج بحثي: "+violations+"");
    var req = repo.save(new ResearchRequest(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), p==null?null:p.userId(), r.purpose(), String.join(",", r.fields())));
    audit.record("research.request.submitted","research_request",req.getId().toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @PostMapping("/requests/{id}/decide") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var req = repo.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    String decision = body.getOrDefault("decision","");
    if (decision.isBlank()) throw BizException.badRequest("decision_required","قرار لجنة الأخلاقيات/الحوكمة موثق إلزاميًا");
    boolean approve = Boolean.parseBoolean(body.getOrDefault("approve","false"));
    req.decide(decision, approve ? "approved" : "rejected"); repo.save(req);
    audit.record("research.request."+(approve?"approved":"rejected"),"research_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @PostMapping("/requests/{id}/extract") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> extract(@PathVariable UUID id) {
    var req = repo.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    if (!ResearchExtractRules.extractAllowed(req.getStatus(), req.fieldList()))
      throw BizException.conflict("extract_not_allowed","المستخرج يتطلب طلبًا معتمدًا وبلا معرّفات مباشرة");
    req.setStatus("extracted"); repo.save(req);
    audit.record("research.extract.produced","research_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req,"note","مستخرج مجهّل الهوية فقط — الحقول: "+req.fieldList());
  }
  @GetMapping("/requests/pending")
  Map<String,Object> pending() {
    var rows = repo.findByStatusOrderByCreatedAtAsc("submitted");
    return Map.of("ok",true,"requests",rows,"count",rows.size());
  }
}
