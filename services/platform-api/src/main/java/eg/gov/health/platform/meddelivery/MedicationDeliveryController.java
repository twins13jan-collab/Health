package eg.gov.health.platform.meddelivery;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Medication delivery to the patient (module 152). Delivery
 *  eligibility and the prescription are verified first; a
 *  pharmacist reviews and approves before preparation and
 *  dispatch. Transport and cold chain are managed end to end:
 *  special-handling medication is handed over only against
 *  verified identity, and a cold-chain breach voids the delivery.
 *  Failed deliveries and returns are managed explicitly — two
 *  failures open the return workflow, and a temperature-breached
 *  pack is quarantined, never restocked. */
@RestController @RequestMapping("/api/medication-delivery")
public class MedicationDeliveryController {
  private final DeliveryOrderRepository orders; private final MedDeliveryAttemptRepository attempts;
  private final DeliveryReturnRepository returns; private final AuditService audit;
  public MedicationDeliveryController(DeliveryOrderRepository orders, MedDeliveryAttemptRepository attempts,
                                      DeliveryReturnRepository returns, AuditService audit) {
    this.orders=orders; this.attempts=attempts; this.returns=returns; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DeliveryOrder order(UUID id) {
    return orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","طلب التوصيل غير موجود"));
  }

  public record OrderRequest(String patientRef, String prescriptionRef, String medicationName, Boolean eligibilityVerified,
                             Boolean specialHandling, Boolean coldChainRequired, String addressRef) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody OrderRequest r) {
    var p = principal();
    var o = orders.save(new DeliveryOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.prescriptionRef(), r.medicationName(),
      Boolean.TRUE.equals(r.eligibilityVerified()), Boolean.TRUE.equals(r.specialHandling()), Boolean.TRUE.equals(r.coldChainRequired()), r.addressRef()));
    audit.record("delivery.order.created","delivery_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  public record ReviewRequest(Boolean approved) {}
  @PostMapping("/orders/{id}/pharmacist-review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody ReviewRequest r) {
    var p = principal(); var o = order(id);
    o.review(p.email(), Boolean.TRUE.equals(r.approved())); orders.save(o);
    audit.record("delivery.pharmacist.reviewed","delivery_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/prepare")
  Map<String,Object> prepare(@PathVariable UUID id) {
    principal(); var o = order(id);
    if (!o.isPharmacistApproved()) throw BizException.conflict("not_approved","التجهيز بعد اعتماد الصيدلي");
    o.setStatus("prepared"); orders.save(o);
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/dispatch")
  Map<String,Object> dispatch(@PathVariable UUID id) {
    principal(); var o = order(id);
    if (!DeliveryRules.dispatchPermitted(o.isEligibilityVerified(), o.isPharmacistApproved(), o.getStatus()))
      throw BizException.conflict("dispatch_blocked","الإرسال يتطلب أهلية توصيل متحققًا منها واعتماد صيدلي وطلبًا مجهزًا");
    o.setStatus("dispatched"); orders.save(o);
    audit.record("delivery.dispatched","delivery_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  public record AttemptRequest(String outcome, String reason, Boolean temperatureOk, String receivedBy, Boolean identityVerified) {}
  @PostMapping("/orders/{id}/attempts")
  Map<String,Object> attempt(@PathVariable UUID id, @RequestBody AttemptRequest r) {
    var p = principal(); var o = order(id);
    if (!"dispatched".equals(o.getStatus())) throw BizException.conflict("not_dispatched","محاولات التسليم على طلب مرسل");
    int no = (int) attempts.countByOrderId(id) + 1;
    if ("delivered".equals(r.outcome()) && !DeliveryRules.handoverValid(o.isSpecialHandling(), Boolean.TRUE.equals(r.identityVerified()), r.temperatureOk(), o.isColdChainRequired()))
      throw BizException.conflict("handover_invalid","تسليم خاص يتطلب هوية متحققًا منها، وكسر سلسلة التبريد يبطل التسليم — العبوة تعود ولا تُترك");
    var a = attempts.save(new MedDeliveryAttempt(UUID.fromString(p.facilityId()), p.governorate(), id, no, r.outcome(), r.reason(),
      r.temperatureOk(), r.receivedBy(), Boolean.TRUE.equals(r.identityVerified())));
    if ("delivered".equals(r.outcome())) o.setStatus("delivered");
    else if (DeliveryRules.returnRequired(attempts.countByOrderIdAndOutcome(id, "failed"))) o.setStatus("failed");
    orders.save(o);
    audit.record("delivery.attempt."+r.outcome(),"delivery_attempt",a.getId().toString(),"{}");
    return Map.of("ok",true,"attempt",a,"order_status",o.getStatus(),
      "return_workflow_open","failed".equals(o.getStatus()));
  }
  public record ReturnRequest(String reason, Boolean temperatureOk) {}
  @PostMapping("/orders/{id}/return")
  Map<String,Object> doReturn(@PathVariable UUID id, @RequestBody ReturnRequest r) {
    var p = principal(); var o = order(id);
    String disposition = DeliveryRules.returnDisposition(o.isColdChainRequired(), Boolean.TRUE.equals(r.temperatureOk()));
    var ret = returns.save(new DeliveryReturn(UUID.fromString(p.facilityId()), p.governorate(), id, r.reason(), disposition, p.email()));
    o.setStatus("returned"); orders.save(o);
    audit.record("delivery.returned","delivery_return",ret.getId().toString(),"{}");
    return Map.of("ok",true,"return_record",ret,"disposition",disposition,
      "note","quarantine".equals(disposition) ? "temperature-breached packs are quarantined — never restocked" : "pack may return to stock after pharmacist check");
  }
}
