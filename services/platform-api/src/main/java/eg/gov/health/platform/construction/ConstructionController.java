package eg.gov.health.platform.construction;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.util.*;
/** Health-facility construction & handover (module 172). Need
 *  studies against the health map; medical, engineering and
 *  infection-control design approvals; contractor contracts and
 *  change orders that apply only when approved; commissioning
 *  tests of medical systems and infrastructure; and handover of
 *  assets, documents and licences — accepted only when every
 *  critical system has passed and everything is received. */
@RestController @RequestMapping("/api/construction")
public class ConstructionController {
  private final ConstructionProjectRepository projects; private final DesignApprovalRepository designs;
  private final ContractChangeOrderRepository changes; private final CommissioningTestRepository tests;
  private final HandoverRecordRepository handovers; private final AuditService audit;
  public ConstructionController(ConstructionProjectRepository projects, DesignApprovalRepository designs, ContractChangeOrderRepository changes,
                                CommissioningTestRepository tests, HandoverRecordRepository handovers, AuditService audit) {
    this.projects=projects; this.designs=designs; this.changes=changes; this.tests=tests; this.handovers=handovers; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ConstructionProject project(UUID id) {
    return projects.findById(id).orElseThrow(() -> BizException.notFound("project_not_found","المشروع غير موجود"));
  }

  public record ProjectRequest(String name, String location, String needStudyRef, String healthMapRef) {}
  @PostMapping("/projects")
  Map<String,Object> create(@RequestBody ProjectRequest r) {
    var p = principal();
    var pr = projects.save(new ConstructionProject(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.location(), r.needStudyRef(), r.healthMapRef()));
    audit.record("construction.project.created","construction_project",pr.getId().toString(),"{}");
    return Map.of("ok",true,"project",pr);
  }
  public record DesignRequest(String discipline, Boolean approved) {}
  @PostMapping("/projects/{id}/design-approvals")
  Map<String,Object> design(@PathVariable UUID id, @RequestBody DesignRequest r) {
    var p = principal(); project(id);
    var d = designs.save(new DesignApproval(UUID.fromString(p.facilityId()), p.governorate(), id, r.discipline(), p.email(), Boolean.TRUE.equals(r.approved())));
    return Map.of("ok",true,"approval",d);
  }
  @PostMapping("/projects/{id}/start-construction")
  Map<String,Object> start(@PathVariable UUID id) {
    principal(); var pr = project(id);
    boolean medical = designs.existsByProjectIdAndDisciplineAndApprovedTrue(id, "medical");
    boolean ipc = designs.existsByProjectIdAndDisciplineAndApprovedTrue(id, "infection_control");
    if (!ConstructionRules.constructionStartPermitted(pr.getNeedStudyRef(), medical, ipc))
      throw BizException.conflict("start_blocked","بدء الإنشاء يتطلب دراسة احتياج موثقة واعتماد التصميم الطبي وتصميم مكافحة العدوى — مكافحة العدوى تراجع الرسومات لا الجدران المنتهية");
    pr.setStatus("construction"); projects.save(pr);
    audit.record("construction.started","construction_project",id.toString(),"{}");
    return Map.of("ok",true,"project",pr);
  }
  public record ChangeRequest(String description, BigDecimal costDelta, Boolean approved) {}
  @PostMapping("/projects/{id}/change-orders")
  Map<String,Object> change(@PathVariable UUID id, @RequestBody ChangeRequest r) {
    var p = principal(); project(id);
    var c = changes.save(new ContractChangeOrder(UUID.fromString(p.facilityId()), p.governorate(), id, r.description(), r.costDelta(), p.email(), Boolean.TRUE.equals(r.approved())));
    return Map.of("ok",true,"change_order",c,"applied",false,"note","change orders take effect only through the apply step, and only when approved");
  }
  @PostMapping("/change-orders/{cid}/apply")
  Map<String,Object> applyChange(@PathVariable UUID cid) {
    principal();
    var c = changes.findById(cid).orElseThrow(() -> BizException.notFound("change_not_found","أمر التغيير غير موجود"));
    if (!c.isApproved()) throw BizException.conflict("change_not_approved","أمر تغيير غير معتمد لا يُطبق على العقد");
    c.apply(); changes.save(c);
    return Map.of("ok",true,"change_order",c,"applied",true);
  }
  public record TestRequest(String systemName, String result) {}
  @PostMapping("/projects/{id}/commissioning-tests")
  Map<String,Object> test(@PathVariable UUID id, @RequestBody TestRequest r) {
    var p = principal(); project(id);
    var t = tests.save(new CommissioningTest(UUID.fromString(p.facilityId()), p.governorate(), id, r.systemName(), r.result(), p.email()));
    audit.record("construction.commissioning."+r.systemName(),"commissioning_test",t.getId().toString(),"{}");
    return Map.of("ok",true,"test",t);
  }
  public record HandoverRequest(Boolean assetsReceived, Boolean documentsReceived, Boolean licensesReceived) {}
  @PostMapping("/projects/{id}/handover")
  Map<String,Object> handover(@PathVariable UUID id, @RequestBody HandoverRequest r) {
    var p = principal(); var pr = project(id);
    var latest = new HashMap<String,String>();
    for (var t : tests.findByProjectIdOrderByCreatedAtDesc(id)) latest.putIfAbsent(t.getSystemName(), t.getResult());
    boolean ok = ConstructionRules.handoverPermitted(latest, Boolean.TRUE.equals(r.assetsReceived()),
      Boolean.TRUE.equals(r.documentsReceived()), Boolean.TRUE.equals(r.licensesReceived()));
    if (!ok) throw BizException.conflict("handover_blocked","التسليم يُقبل فقط بنجاح تشغيل كل الأنظمة الحرجة (غازات طبية وكهرباء وتكييف ومياه وشبكة) واستلام الأصول والوثائق والتراخيص — فشل الغازات ليس ملحوظة، إنه مستشفى لا يفتح");
    var h = handovers.save(new HandoverRecord(UUID.fromString(p.facilityId()), p.governorate(), id,
      Boolean.TRUE.equals(r.assetsReceived()), Boolean.TRUE.equals(r.documentsReceived()), Boolean.TRUE.equals(r.licensesReceived()), p.email()));
    pr.setStatus("operational"); projects.save(pr);
    audit.record("construction.handed_over","handover_record",h.getId().toString(),"{}");
    return Map.of("ok",true,"handover",h,"project_status","operational");
  }
}
