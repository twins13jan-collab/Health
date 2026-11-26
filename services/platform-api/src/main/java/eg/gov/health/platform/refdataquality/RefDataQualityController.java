package eg.gov.health.platform.refdataquality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Reference-data quality & semantic linking (module 182).
 *  Unified identifiers for facilities, departments, services
 *  and practitioners; matching between local and reference
 *  codes; conversion maps between vendor systems; unknown or
 *  incompatible values monitored; human review of sensitive
 *  matches; and the history of every map change preserved. */
@RestController @RequestMapping("/api/ref-data-quality")
public class RefDataQualityController {
  private final CodeMappingRepository mappings; private final MappingChangeRepository changes; private final AuditService audit;
  public RefDataQualityController(CodeMappingRepository mappings, MappingChangeRepository changes, AuditService audit) {
    this.mappings=mappings; this.changes=changes; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CodeMapping mapping(UUID id) {
    return mappings.findById(id).orElseThrow(() -> BizException.notFound("mapping_not_found","الربط غير موجود"));
  }

  public record MapBody(String localCode, String referenceCode, String codeSystem, Boolean sensitive, Boolean humanReviewed) {}
  @PostMapping("/mappings")
  Map<String,Object> create(@RequestBody MapBody r) {
    var p = principal();
    var m = mappings.save(new CodeMapping(UUID.fromString(p.facilityId()), p.governorate(), r.localCode(), r.referenceCode(),
      r.codeSystem(), Boolean.TRUE.equals(r.sensitive()), Boolean.TRUE.equals(r.humanReviewed())));
    return Map.of("ok",true,"mapping",m);
  }
  @PostMapping("/mappings/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var m = mapping(id);
    if (!RefDataRules.activatable(m.isSensitive(), m.isHumanReviewed()))
      throw BizException.conflict("human_review_required","تطابق رمز حساس (تشخيص أو سبب وفاة) لا يُفعّل قبل مراجعة بشرية — مطابقة آلية واسعة هي كيف يصير رمز خاطئ حقيقة رسمية في آلاف السجلات دفعة واحدة");
    m.activate(); mappings.save(m);
    return Map.of("ok",true,"mapping",m);
  }
  public record RemapBody(String newReferenceCode, String reason) {}
  @PostMapping("/mappings/{id}/remap")
  Map<String,Object> remap(@PathVariable UUID id, @RequestBody RemapBody r) {
    var p = principal(); var m = mapping(id);
    if (r.reason()==null || r.reason().isBlank())
      throw BizException.badRequest("reason_required","تغيير خريطة أكواد يحتفظ بتاريخه: القيمة القديمة والجديدة والسبب");
    var ch = changes.save(new MappingChange(UUID.fromString(p.facilityId()), p.governorate(), id, m.getReferenceCode(), r.newReferenceCode(), r.reason()));
    m.remap(r.newReferenceCode()); mappings.save(m);
    return Map.of("ok",true,"change",ch,"mapping",m);
  }
}
