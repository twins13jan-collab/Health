package eg.gov.health.platform.clinicalforms;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Structured clinical forms & documents (module 138).
 *  Templates for assessments, consents and reports; fields
 *  defined per specialty and service; author, signature and
 *  timing recorded; templates and their versions managed; and
 *  forms converted into structured data. */
@RestController @RequestMapping("/api/clinical-forms")
public class ClinicalFormsController {
  private final FormTemplateRepository templates; private final FormInstanceRepository instances; private final AuditService audit;
  public ClinicalFormsController(FormTemplateRepository templates, FormInstanceRepository instances, AuditService audit) {
    this.templates=templates; this.instances=instances; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private FormTemplate template(UUID id) {
    return templates.findById(id).orElseThrow(() -> BizException.notFound("template_not_found","القالب غير موجود"));
  }

  public record TemplateBody(String name, String specialty, String fieldsSpec, String version) {}
  @PostMapping("/templates")
  Map<String,Object> create(@RequestBody TemplateBody r) {
    var p = principal();
    var t = templates.save(new FormTemplate(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.specialty(), r.fieldsSpec(), r.version()));
    return Map.of("ok",true,"template",t);
  }
  @PostMapping("/templates/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var t = template(id);
    t.setStatus("active"); templates.save(t);
    return Map.of("ok",true,"template",t);
  }
  @PostMapping("/templates/{id}/retire")
  Map<String,Object> retire(@PathVariable UUID id) {
    principal(); var t = template(id);
    t.setStatus("retired"); templates.save(t);
    return Map.of("ok",true,"template",t);
  }
  public record InstanceBody(String patientRef, String authorRef, UUID supersedesId) {}
  @PostMapping("/templates/{id}/instances")
  Map<String,Object> instantiate(@PathVariable UUID id, @RequestBody InstanceBody r) {
    var p = principal(); var t = template(id);
    if (!FormRules.instantiable(t.getStatus()))
      throw BizException.conflict("template_not_active","الكتابة على إصدار قالب نشط فقط — لا مسودة ولا متقاعد");
    var inst = instances.save(new FormInstance(UUID.fromString(p.facilityId()), p.governorate(), id, r.patientRef(), r.authorRef(), r.supersedesId()));
    if (r.supersedesId() != null) {
      instances.findById(r.supersedesId()).ifPresent(old -> { old.supersede(); instances.save(old); });
    }
    return Map.of("ok",true,"instance",inst);
  }
  @PostMapping("/instances/{iid}/sign")
  Map<String,Object> sign(@PathVariable UUID iid) {
    principal();
    var inst = instances.findById(iid).orElseThrow(() -> BizException.notFound("instance_not_found","النموذج غير موجود"));
    inst.sign(); instances.save(inst);
    return Map.of("ok",true,"instance",inst);
  }
  @PostMapping("/instances/{iid}/file")
  Map<String,Object> file(@PathVariable UUID iid) {
    principal();
    var inst = instances.findById(iid).orElseThrow(() -> BizException.notFound("instance_not_found","النموذج غير موجود"));
    if (!FormRules.filable(inst.isSigned()))
      throw BizException.conflict("unsigned_form","نموذج غير موقع لا يُحفظ نهائيًا في السجل");
    inst.file(); instances.save(inst);
    audit.record("forms.instance.filed","form_instance",iid.toString(),"{}");
    return Map.of("ok",true,"instance",inst,"structured_data",true);
  }
}
