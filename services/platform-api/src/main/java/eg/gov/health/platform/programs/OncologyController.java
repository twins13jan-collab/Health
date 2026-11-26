package eg.gov.health.platform.programs;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/oncology")
public class OncologyController {
  private final ChemoCycleRepository repo; private final AuditService audit;
  public OncologyController(ChemoCycleRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record CycleRequest(UUID patientId, String protocol, String drugCode, Integer cycleNumber,
                             Double heightCm, Double weightKg, BigDecimal mgPerM2, Integer creatinineClearance, LocalDate scheduledDate) {}
  @PostMapping("/cycles")
  Map<String,Object> plan(@RequestBody CycleRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.drugCode()==null||r.heightCm()==null||r.weightKg()==null||r.mgPerM2()==null||r.creatinineClearance()==null)
      throw BizException.badRequest("missing_field","بيانات الجرعة ناقصة");
    double bsa = ChemoDosing.bsaMosteller(r.heightCm(), r.weightKg());
    var pct = ChemoDosing.renalAdjustmentPct(r.creatinineClearance());
    if (pct.signum()==0)
      throw BizException.conflict("renal_review_required","تصفية كرياتينين أقل من 25 — لا جرعة آلية؛ مراجعة استشاري أورام وكلى إلزامية");
    var dose = ChemoDosing.adjustedDoseMg(bsa, r.mgPerM2(), r.creatinineClearance());
    var c = repo.save(new ChemoCycle(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.protocol(), r.drugCode(),
      r.cycleNumber()==null?1:r.cycleNumber(), Math.round(bsa*100.0)/100.0, dose, r.scheduledDate()));
    audit.record("oncology.cycle.planned","chemo_cycle",c.getId().toString(),"{}");
    return Map.of("ok",true,"cycle",c,"bsa",Math.round(bsa*100.0)/100.0,"renal_adjustment_pct",pct,"dose_mg",dose,
      "note","جرعة محسوبة استشاريًا — تتطلب تحقق صيدلي سريري مستقل قبل التحضير");
  }
  @PostMapping("/cycles/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id) {
    var p = TenantContext.get();
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("cycle_not_found","الدورة غير موجودة"));
    if (!"planned".equals(c.getStatus())) throw BizException.conflict("not_planned","التحقق من حالة التخطيط فقط");
    c.verify(p==null?null:p.userId()); repo.save(c);
    audit.record("oncology.cycle.verified","chemo_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",c);
  }
  @PostMapping("/cycles/{id}/administer")
  Map<String,Object> administer(@PathVariable UUID id) {
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("cycle_not_found","الدورة غير موجودة"));
    if (!"verified".equals(c.getStatus())) throw BizException.conflict("not_verified","لا إعطاء قبل تحقق صيدلي موثق — قاعدة سلامة مطلقة");
    c.setStatus("administered"); repo.save(c);
    audit.record("oncology.cycle.administered","chemo_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",c);
  }
  @GetMapping("/cycles")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByCycleNumberAsc(patientId);
    return Map.of("ok",true,"cycles",rows,"count",rows.size());
  }
}
