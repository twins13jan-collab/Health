package eg.gov.health.platform.researchgovernance;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Research governance & secondary data (module 65). Data-use
 *  requests for research, planning, evaluation and public
 *  policy; review of purpose, necessity, the minimum dataset,
 *  legal basis and required approvals; aggregate or
 *  de-identified data used whenever appropriate; ethics
 *  committees, access permits, researchers and study outputs
 *  managed; re-identification prevented and export and later
 *  use monitored; and data sharing with universities and
 *  research bodies under clear agreements. */
@RestController @RequestMapping("/api/research-governance")
public class ResearchGovernanceController {
  private final ResearchDataRequestRepository requests; private final AuditService audit;
  public ResearchGovernanceController(ResearchDataRequestRepository requests, AuditService audit) {
    this.requests=requests; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ResearchDataRequest request(UUID id) {
    return requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
  }

  public record ReqBody(String purpose, Boolean necessityJustified, Boolean minimalDataset, String legalBasis,
                        Boolean ethicsApproved, String dataForm, Boolean identifiedJustified) {}
  @PostMapping("/requests")
  Map<String,Object> create(@RequestBody ReqBody r) {
    var p = principal();
    var q = requests.save(new ResearchDataRequest(UUID.fromString(p.facilityId()), p.governorate(), r.purpose(),
      Boolean.TRUE.equals(r.necessityJustified()), Boolean.TRUE.equals(r.minimalDataset()), r.legalBasis(),
      Boolean.TRUE.equals(r.ethicsApproved()), r.dataForm(), Boolean.TRUE.equals(r.identifiedJustified())));
    return Map.of("ok",true,"request",q);
  }
  @PostMapping("/requests/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id) {
    principal(); var q = request(id);
    var blockers = ResearchRules.approvalBlockers(q.getPurpose(), q.isNecessityJustified(), q.isMinimalDataset(), q.getLegalBasis(),
      q.isEthicsApproved(), q.getDataForm(), q.isIdentifiedJustified());
    if (!blockers.isEmpty())
      throw BizException.conflict("not_approvable","طلب بيانات بحثية يقف على: " + String.join("، ", blockers) + " — موافقة ناقصة ركن منها انتزاع بيانات بعنوان دراسة");
    q.approve(); requests.save(q);
    audit.record("research.request.approved","research_data_request",id.toString(),"{}");
    return Map.of("ok",true,"request",q);
  }
  @PostMapping("/requests/{id}/export")
  Map<String,Object> export(@PathVariable UUID id) {
    principal(); var q = request(id);
    if (!"approved".equals(q.getStatus()))
      throw BizException.conflict("export_blocked","تصدير بيانات بحثية لطلب معتمد فقط وتحت المراقبة — الموافقة التي منحت للرعاية لم تكن موافقة على كل شيء");
    audit.record("research.data.exported","research_data_request",id.toString(),"{}");
    return Map.of("ok",true,"exported",true,"data_form",q.getDataForm(),
      "reidentification_prohibited",true);
  }
}
