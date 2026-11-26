package eg.gov.health.platform.dental;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Dentistry & oral health (cycle 248). Charts are versioned; procedures
 *  trace to tooth + dentist + material + lot; no procedure on an
 *  unverified site, without consent, with an unreviewed anaesthesia risk,
 *  or with instruments from a failed/expired sterilization cycle; a
 *  recalled implant lot traces back to every affected patient. */
@RestController @RequestMapping("/api/dental")
public class DentalController {
  private final DentalChartRepository charts; private final DentalChartEntryRepository entries;
  private final DentalTreatmentPlanRepository plans; private final SterilizationCycleRepository sterilization;
  private final DentalProcedureRepository procedures; private final DentalImageRepository images;
  private final ImplantRecordRepository implants; private final AuditService audit;
  public DentalController(DentalChartRepository charts, DentalChartEntryRepository entries,
                          DentalTreatmentPlanRepository plans, SterilizationCycleRepository sterilization,
                          DentalProcedureRepository procedures, DentalImageRepository images,
                          ImplantRecordRepository implants, AuditService audit) {
    this.charts=charts; this.entries=entries; this.plans=plans; this.sterilization=sterilization;
    this.procedures=procedures; this.images=images; this.implants=implants; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  @PostMapping("/charts")
  Map<String,Object> newChart(@RequestBody Map<String,String> body) {
    var p = principal();
    String pid = body.get("patientId");
    if (pid==null) throw BizException.badRequest("patient_required","المريض مطلوب");
    UUID patientId = UUID.fromString(pid);
    var active = charts.findByPatientIdAndStatusOrderByVersionDesc(patientId, "active");
    int next = active.isEmpty() ? 1 : active.get(0).getVersion() + 1;
    active.forEach(c -> { c.supersede(); charts.save(c); });
    var c = charts.save(new DentalChart(UUID.fromString(p.facilityId()), p.governorate(), patientId, next));
    audit.record("dental.chart.versioned","dental_chart",c.getId().toString(),"{}");
    return Map.of("ok",true,"chart",c,"version",next);
  }
  public record EntryRequest(Integer toothNumber, String condition, String surfaces, String notes) {}
  @PostMapping("/charts/{id}/entries")
  Map<String,Object> entry(@PathVariable UUID id, @RequestBody EntryRequest r) {
    var p = principal();
    var chart = charts.findById(id).orElseThrow(() -> BizException.notFound("chart_not_found","مخطط الأسنان غير موجود"));
    if (!"active".equals(chart.getStatus())) throw BizException.conflict("chart_superseded","المخطط ليس النسخة النشطة");
    if (r.toothNumber()==null || !DentalRules.validFdi(r.toothNumber()))
      throw BizException.badRequest("invalid_fdi","رقم سن غير صحيح بترقيم FDI (ربع 1-8، سن 1-8)");
    var e = entries.save(new DentalChartEntry(chart.getFacilityId(), p.governorate(), id, r.toothNumber(), r.condition(), r.surfaces(), r.notes()));
    audit.record("dental.chart.entry","dental_chart_entry",e.getId().toString(),"{}");
    return Map.of("ok",true,"entry",e);
  }

  public record PlanRequest(UUID patientId, String diagnosis, String plan, Boolean consentDocumented) {}
  @PostMapping("/treatment-plans")
  Map<String,Object> plan(@RequestBody PlanRequest r) {
    var p = principal();
    if (r.patientId()==null||r.diagnosis()==null) throw BizException.badRequest("invalid_plan","المريض والتشخيص مطلوبان");
    var pl = plans.save(new DentalTreatmentPlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.diagnosis(), r.plan(), Boolean.TRUE.equals(r.consentDocumented())));
    audit.record("dental.plan.created","dental_treatment_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }

  public record SterilizationRequest(String instrumentSet, String cycleNumber, String status, OffsetDateTime expiresAt) {}
  @PostMapping("/sterilization-cycles")
  Map<String,Object> sterilization(@RequestBody SterilizationRequest r) {
    var p = principal();
    if (r.instrumentSet()==null||r.status()==null||r.expiresAt()==null)
      throw BizException.badRequest("invalid_cycle","طقم الأدوات ونتيجة الدورة وصلاحيتها مطلوبة");
    var c = sterilization.save(new SterilizationCycle(UUID.fromString(p.facilityId()), p.governorate(),
      r.instrumentSet(), r.cycleNumber(), r.status(), r.expiresAt()));
    audit.record("dental.sterilization.recorded","sterilization_cycle",c.getId().toString(),"{}");
    return Map.of("ok",true,"cycle",c);
  }

  public record ProcedureRequest(UUID patientId, UUID planId, Integer toothNumber, Boolean siteVerified,
                                 String procedureCode, String anesthesiaType, Boolean anesthesiaRisk, Boolean physicianReviewed,
                                 String materials, String materialLot, UUID sterilizationCycleId, String dentist,
                                 String complications, String dischargeInstructions) {}
  @PostMapping("/procedures")
  Map<String,Object> procedure(@RequestBody ProcedureRequest r) {
    var p = principal();
    if (r.patientId()==null||r.procedureCode()==null||r.toothNumber()==null)
      throw BizException.badRequest("invalid_procedure","المريض والإجراء والسن مطلوبة");
    // Uncertain tooth/site: nothing starts before verification.
    if (!DentalRules.siteReady(Boolean.TRUE.equals(r.siteVerified()), r.toothNumber()))
      throw BizException.badRequest("site_not_verified","لا إجراء قبل التحقق من السن/الموقع بترقيم FDI صحيح (دورة 248)");
    if (r.planId()!=null) {
      var pl = plans.findById(r.planId()).orElseThrow(() -> BizException.notFound("plan_not_found","خطة العلاج غير موجودة"));
      if (!pl.isConsentDocumented())
        throw BizException.conflict("consent_required","إجراء بلا موافقة موثقة على الخطة — الموافقة تسبق العلاج");
    }
    if (!DentalRules.anesthesiaCleared(Boolean.TRUE.equals(r.anesthesiaRisk()), Boolean.TRUE.equals(r.physicianReviewed())))
      throw BizException.badRequest("anesthesia_review_required","خطر تخدير موثق يتطلب مراجعة طبيب الأسنان قبل الإجراء");
    if (r.sterilizationCycleId()!=null) {
      var cyc = sterilization.findById(r.sterilizationCycleId())
        .orElseThrow(() -> BizException.notFound("cycle_not_found","دورة التعقيم غير موجودة"));
      if (!DentalRules.sterilizationValid(cyc.getStatus(), cyc.getExpiresAt(), OffsetDateTime.now()))
        throw BizException.conflict("sterilization_blocked","أدوات من دورة تعقيم فاشلة أو منتهية محظورة — إعادة معالجة لازمة (دورة 248)");
    }
    String status = (r.complications()!=null && !r.complications().isBlank()) ? "complicated" : "performed";
    var proc = procedures.save(new DentalProcedure(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.planId(),
      r.toothNumber(), true, r.procedureCode(), r.anesthesiaType(), Boolean.TRUE.equals(r.anesthesiaRisk()),
      Boolean.TRUE.equals(r.physicianReviewed()), r.materials(), r.materialLot(), r.sterilizationCycleId(),
      r.dentist(), r.complications(), r.dischargeInstructions(), status));
    audit.record("dental.procedure.performed","dental_procedure",proc.getId().toString(),"{}");
    return Map.of("ok",true,"procedure",proc);
  }

  public record ImageRequest(UUID patientId, UUID procedureId, String imageType, String device, String region) {}
  @PostMapping("/images")
  Map<String,Object> image(@RequestBody ImageRequest r) {
    var p = principal();
    if (r.patientId()==null||r.imageType()==null) throw BizException.badRequest("invalid_image","المريض ونوع الصورة مطلوبان");
    var img = images.save(new DentalImage(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.procedureId(), r.imageType(), r.device(), r.region()));
    audit.record("dental.image.linked","dental_image",img.getId().toString(),"{}");
    return Map.of("ok",true,"image",img);
  }

  public record ImplantRequest(UUID patientId, String manufacturer, String lotNumber, String serialNumber, Integer toothNumber) {}
  @PostMapping("/implants")
  Map<String,Object> implant(@RequestBody ImplantRequest r) {
    var p = principal();
    if (r.patientId()==null||r.manufacturer()==null||r.lotNumber()==null)
      throw BizException.badRequest("invalid_implant","المريض والشركة ورقم التشغيلة مطلوبة للتتبع");
    if (r.toothNumber()!=null && !DentalRules.validFdi(r.toothNumber()))
      throw BizException.badRequest("invalid_fdi","رقم سن غير صحيح بترقيم FDI");
    var im = implants.save(new ImplantRecord(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.manufacturer(), r.lotNumber(), r.serialNumber(), r.toothNumber()));
    audit.record("dental.implant.registered","implant_record",im.getId().toString(),"{}");
    return Map.of("ok",true,"implant",im);
  }
  @PostMapping("/implants/recall")
  Map<String,Object> recall(@RequestBody Map<String,String> body) {
    String lot = body.get("lotNumber");
    if (lot==null) throw BizException.badRequest("lot_required","رقم التشغيلة مطلوب");
    var affected = implants.findByLotNumber(lot);
    affected.forEach(im -> { if ("implanted".equals(im.getStatus())) { im.recall(); implants.save(im); } });
    var patients = affected.stream().map(ImplantRecord::getPatientId).distinct().toList();
    audit.record("dental.implant.recalled","implant_record",lot,"{}");
    return Map.of("ok",true,"lot",lot,"affected_implants",affected.size(),"affected_patients",patients,
      "action","trace and contact every affected patient — recall is traceable by lot");
  }

  @GetMapping("/procedures")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = procedures.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"procedures",rows,"count",rows.size());
  }
}
