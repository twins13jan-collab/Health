package eg.gov.health.platform.radiologydeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Radiology & medical imaging (module 93). The exam order,
 *  its justification, priority and contraindications are
 *  managed; preparation, contrast, kidney risk and reactions
 *  are governed; modality worklists run; preliminary and final
 *  reports, corrections and sign-off are controlled; critical
 *  results are communicated with documented receipt; images
 *  link through DICOM, DICOMweb and PACS; and radiation doses
 *  and repeat exams are tracked. */
@RestController @RequestMapping("/api/radiology-deep")
public class RadiologyDeepController {
  private final ImagingOrderRepository orders; private final ImagingReportRepository reports;
  private final ImagingCriticalResultRepository criticals; private final AuditService audit;
  public RadiologyDeepController(ImagingOrderRepository orders, ImagingReportRepository reports,
                                 ImagingCriticalResultRepository criticals, AuditService audit) {
    this.orders=orders; this.reports=reports; this.criticals=criticals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ImagingOrder order(UUID id) {
    return orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
  }

  public record OrderBody(String patientRef, String exam, String justification, String priority,
                          Boolean contraindicationsChecked, Boolean contrastPlanned, Double egfrValue, Boolean nephrologyOverride) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody OrderBody r) {
    var p = principal();
    var o = orders.save(new ImagingOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.exam(), r.justification(),
      r.priority(), Boolean.TRUE.equals(r.contraindicationsChecked()), Boolean.TRUE.equals(r.contrastPlanned()), r.egfrValue(),
      Boolean.TRUE.equals(r.nephrologyOverride())));
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/perform")
  Map<String,Object> perform(@PathVariable UUID id) {
    principal(); var o = order(id);
    if (!RadiologyRules.contrastPermitted(o.isContraindicationsChecked(), o.isContrastPlanned(), o.getEgfrValue(), o.isNephrologyOverride()))
      throw BizException.conflict("contrast_blocked","فحص بالصبغة لا يُنفذ قبل فحص الموانع وتسجيل وظائف الكلى — ودون eGFR 30 يحتاج تجاوز كلى موثقًا: الصبغة قرار كلى قبل أن تكون قرار صورة");
    o.perform(); orders.save(o);
    return Map.of("ok",true,"order",o);
  }
  public record ReportBody(String reportType, String signedBy, UUID supersedesId) {}
  @PostMapping("/orders/{id}/reports")
  Map<String,Object> report(@PathVariable UUID id, @RequestBody ReportBody r) {
    var p = principal(); order(id);
    if ("correction".equals(r.reportType()) && r.supersedesId()==null)
      throw BizException.badRequest("correction_needs_target","تصحيح تقرير نهائي يكون تقريرًا جديدًا يحل محل سابقه بالاسم — التقرير النهائي لا يُحرر أبدًا");
    var rep = reports.save(new ImagingReport(UUID.fromString(p.facilityId()), p.governorate(), id, r.reportType(), r.signedBy(), r.supersedesId()));
    return Map.of("ok",true,"report",rep);
  }
  public record CriticalBody(String finding) {}
  @PostMapping("/orders/{id}/critical-results")
  Map<String,Object> critical(@PathVariable UUID id, @RequestBody CriticalBody r) {
    var p = principal(); order(id);
    var c = criticals.save(new ImagingCriticalResult(UUID.fromString(p.facilityId()), p.governorate(), id, r.finding()));
    return Map.of("ok",true,"critical_result",c,"communicated",false,"note","a critical finding is not communicated until a named receiver acknowledges it");
  }
  public record AckBody(String acknowledgedBy) {}
  @PostMapping("/critical-results/{id}/acknowledge")
  Map<String,Object> ack(@PathVariable UUID id, @RequestBody AckBody r) {
    principal();
    var c = criticals.findById(id).orElseThrow(() -> BizException.notFound("critical_not_found","النتيجة غير موجودة"));
    if (r.acknowledgedBy()==null || r.acknowledgedBy().isBlank())
      throw BizException.badRequest("receiver_required","استلام نتيجة حرجة يحتاج مستقبلًا مسمى");
    c.acknowledge(r.acknowledgedBy()); criticals.save(c);
    return Map.of("ok",true,"critical_result",c,"communicated",true);
  }
  @PostMapping("/orders/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var o = order(id);
    var crits = criticals.findByOrderId(id);
    boolean hasCrit = !crits.isEmpty();
    boolean acked = crits.stream().allMatch(ImagingCriticalResult::isAcknowledged);
    if (!RadiologyRules.orderClosable(hasCrit, acked))
      throw BizException.conflict("critical_unacknowledged","طلب أشعة بنتيجة حرجة لا يُغلق قبل استلام موثق من مستقبل مسمى");
    o.close(); orders.save(o);
    return Map.of("ok",true,"order",o);
  }
}
