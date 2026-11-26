package eg.gov.health.platform.medicalassets;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
/** Medical assets, maintenance & calibration (cycle 230). Every asset
 *  has an identity, owner, location and state with a full lifecycle;
 *  calibration currency is verified before use and an out-of-calibration
 *  device is flagged unfit at integration; the clinician reports faults
 *  but never returns a device to service — biomedical engineering and
 *  quality release it; patient-affecting faults open linked safety
 *  cases; spare-part shortages spawn purchase requests tied to the
 *  downtime. */
@RestController @RequestMapping("/api/medical-assets")
public class MedicalAssetController {
  private final MedicalAssetRepository assets; private final AssetLifecycleEventRepository events;
  private final MaintenancePlanRepository plans; private final WorkOrderRepository workOrders;
  private final CalibrationRecordRepository calibrations; private final DeviceIncidentRepository incidents;
  private final AuditService audit;
  public MedicalAssetController(MedicalAssetRepository assets, AssetLifecycleEventRepository events, MaintenancePlanRepository plans,
                                WorkOrderRepository workOrders, CalibrationRecordRepository calibrations,
                                DeviceIncidentRepository incidents, AuditService audit) {
    this.assets=assets; this.events=events; this.plans=plans; this.workOrders=workOrders;
    this.calibrations=calibrations; this.incidents=incidents; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private MedicalAsset asset(UUID id) {
    return assets.findById(id).orElseThrow(() -> BizException.notFound("asset_not_found","الأصل غير موجود"));
  }
  private void event(MedicalAsset a, String type, String details, String actor) {
    events.save(new AssetLifecycleEvent(a.getFacilityId(), null, a.getId(), type, details, actor));
  }

  public record AssetRequest(String assetCode, String type, String manufacturer, String model, String serialNumber, String location, String owner, LocalDate calibrationDue) {}
  @PostMapping
  Map<String,Object> create(@RequestBody AssetRequest r) {
    var p = principal();
    if (r.assetCode()==null||r.owner()==null||r.location()==null)
      throw BizException.badRequest("invalid_asset","كل أصل له معرف ومالك وموقع وحالة");
    var a = assets.save(new MedicalAsset(UUID.fromString(p.facilityId()), p.governorate(), r.assetCode(), r.type(),
      r.manufacturer(), r.model(), r.serialNumber(), r.location(), r.owner(), r.calibrationDue()));
    event(a, "commission", "registered and commissioned", p.email());
    audit.record("asset.registered","medical_asset",a.getId().toString(),"{}");
    return Map.of("ok",true,"asset",a);
  }
  @GetMapping("/{id}/usability")
  Map<String,Object> usability(@PathVariable UUID id) {
    var a = asset(id);
    boolean usable = AssetRules.usable(a.getStatus(), a.getCalibrationDue(), LocalDate.now());
    return Map.of("ok",true,"asset_id",id,"status",a.getStatus(),"calibration_due",String.valueOf(a.getCalibrationDue()),
      "usable",usable,"flag", usable ? "fit for use" : "UNFIT — out of calibration or out of service (flagged at integration)");
  }
  @PostMapping("/{id}/transfer")
  Map<String,Object> transfer(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var a = asset(id);
    if (body.get("location")==null) throw BizException.badRequest("location_required","نقل الجهاز يحدث الموقع والحيازة");
    String from = a.getLocation();
    a.transfer(body.get("location")); assets.save(a);
    event(a, "transfer", from + " -> " + body.get("location"), p.email());
    audit.record("asset.transferred","medical_asset",id.toString(),"{}");
    return Map.of("ok",true,"asset",a,"custody_updated",true);
  }
  public record PlanRequest(String planType, Integer intervalDays, LocalDate nextDue) {}
  @PostMapping("/{id}/maintenance-plans")
  Map<String,Object> plan(@PathVariable UUID id, @RequestBody PlanRequest r) {
    var p = principal(); var a = asset(id);
    var pl = plans.save(new MaintenancePlan(a.getFacilityId(), p.governorate(), id, r.planType(), r.intervalDays(), r.nextDue()));
    audit.record("asset.plan.created","maintenance_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  public record WorkOrderRequest(String type, String technician) {}
  @PostMapping("/{id}/work-orders")
  Map<String,Object> workOrder(@PathVariable UUID id, @RequestBody WorkOrderRequest r) {
    var p = principal(); var a = asset(id);
    var w = workOrders.save(new WorkOrder(a.getFacilityId(), p.governorate(), id, r.type(), r.technician()));
    a.setStatus("under_maintenance"); assets.save(a);
    event(a, "maintenance", "work order opened: " + r.type(), p.email());
    audit.record("asset.work_order.opened","work_order",w.getId().toString(),"{}");
    return Map.of("ok",true,"work_order",w);
  }
  public record CompleteRequest(String spareParts, Boolean sparePartShortage, String sparePurchaseRef, BigDecimal downtimeHours, String result) {}
  @PostMapping("/work-orders/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id, @RequestBody CompleteRequest r) {
    var w = workOrders.findById(id).orElseThrow(() -> BizException.notFound("work_order_not_found","أمر العمل غير موجود"));
    boolean shortage = Boolean.TRUE.equals(r.sparePartShortage());
    // A missing spare part spawns a purchase request and ties downtime to inventory.
    if (shortage && (r.sparePurchaseRef()==null||r.sparePurchaseRef().isBlank()))
      throw BizException.badRequest("spare_ref_required","نقص قطعة الغيار ينشئ طلب شراء ويربط التوقف بالمخزون — مرجع الطلب مطلوب");
    w.complete(r.spareParts(), shortage, r.sparePurchaseRef(), r.downtimeHours(), r.result()); workOrders.save(w);
    audit.record("asset.work_order.completed","work_order",id.toString(),"{}");
    return Map.of("ok",true,"work_order",w,"purchase_request_created",shortage);
  }
  /** Return to service is released by biomedical engineering/quality — never by the clinical reporter. */
  @PostMapping("/work-orders/{id}/release")
  Map<String,Object> release(@PathVariable UUID id) {
    var p = principal();
    var w = workOrders.findById(id).orElseThrow(() -> BizException.notFound("work_order_not_found","أمر العمل غير موجود"));
    if (!AssetRules.mayRelease(p.role()))
      throw BizException.forbidden("المستخدم السريري يبلغ عن العطل ولا يعيد الجهاز للخدمة — الإفراج للهندسة الطبية والجودة");
    if (!"completed".equals(w.getStatus())) throw BizException.conflict("not_completed","الإفراج بعد إتمام أمر العمل");
    w.release(p.email()); workOrders.save(w);
    var a = asset(w.getAssetId()); a.setStatus("in_service"); assets.save(a);
    event(a, "maintenance", "released to service by " + p.email(), p.email());
    audit.record("asset.released","work_order",id.toString(),"{}");
    return Map.of("ok",true,"work_order",w,"asset_status","in_service");
  }
  public record CalibrationRequest(String referenceStandard, String certificateRef, String deviation, Boolean accepted, LocalDate dueDate) {}
  @PostMapping("/{id}/calibrations")
  Map<String,Object> calibrate(@PathVariable UUID id, @RequestBody CalibrationRequest r) {
    var p = principal(); var a = asset(id);
    if (!AssetRules.mayRelease(p.role()))
      throw BizException.forbidden("اعتماد المعايرة والإفراج للهندسة الطبية والجودة حسب الاختصاص");
    boolean accepted = Boolean.TRUE.equals(r.accepted());
    var rec = calibrations.save(new CalibrationRecord(a.getFacilityId(), p.governorate(), id, r.referenceStandard(),
      r.certificateRef(), r.deviation(), accepted, r.dueDate(), p.email()));
    if (accepted && r.dueDate()!=null) { a.setCalibrationDue(r.dueDate()); assets.save(a); }
    if (!accepted) { a.setStatus("quarantined"); assets.save(a); }
    event(a, "calibration", accepted ? "accepted, due " + r.dueDate() : "REJECTED — device quarantined", p.email());
    audit.record("asset.calibrated","calibration_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"calibration",rec,"asset_status",a.getStatus());
  }
  public record IncidentRequest(String description, Boolean patientAffected, String safetyCaseRef) {}
  @PostMapping("/{id}/incidents")
  Map<String,Object> incident(@PathVariable UUID id, @RequestBody IncidentRequest r) {
    return doIncident(asset(id), r, principal());
  }
  private Map<String,Object> doIncident(MedicalAsset a, IncidentRequest r, TenantContext.Principal p) {
    boolean affected = Boolean.TRUE.equals(r.patientAffected());
    if (!AssetRules.safetyCaseRequired(affected, r.safetyCaseRef()))
      throw BizException.badRequest("safety_case_required","عطل مؤثر على مريض يفتح بلاغ سلامة مرتبطًا بالحالة — مرجع البلاغ مطلوب");
    var inc = incidents.save(new DeviceIncident(a.getFacilityId(), p.governorate(), a.getId(), p.email(), r.description(), affected, r.safetyCaseRef()));
    a.setStatus("out_of_service"); assets.save(a);
    event(a, "maintenance", "device incident reported; device out of service", p.email());
    audit.record("asset.incident.reported","device_incident",inc.getId().toString(),"{}");
    return Map.of("ok",true,"incident",inc,"asset_status","out_of_service",
      "note","the reporter cannot return this device to service — release is biomedical/quality only");
  }
  /** Biomedical operations board: every asset that needs
   *  engineering attention, ranked (patient-affecting incident →
   *  out of service → out of calibration → quarantined → under
   *  maintenance → falling due), plus the open work-order queue
   *  and what awaits release back to service. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    UUID fid = UUID.fromString(p.facilityId());
    LocalDate today = LocalDate.now();
    var allAssets = assets.findByFacilityId(fid);
    var allPlans = plans.findByFacilityId(fid);
    var allWos = workOrders.findByFacilityId(fid);
    var allIncs = incidents.findByFacilityId(fid);
    Map<UUID, LocalDate> nextPlanDue = new HashMap<>();
    for (var pl : allPlans) if (pl.getNextDue() != null)
      nextPlanDue.merge(pl.getAssetId(), pl.getNextDue(), (a, b) -> a.isBefore(b) ? a : b);
    Map<UUID, Long> openWoByAsset = new HashMap<>(), openIncByAsset = new HashMap<>();
    Set<UUID> patientIncAssets = new HashSet<>();
    for (var w : allWos) if ("open".equals(w.getStatus())) openWoByAsset.merge(w.getAssetId(), 1L, Long::sum);
    for (var i : allIncs) if ("open".equals(i.getStatus())) {
      openIncByAsset.merge(i.getAssetId(), 1L, Long::sum);
      if (i.isPatientAffected()) patientIncAssets.add(i.getAssetId());
    }
    List<Map<String,Object>> queue = new ArrayList<>();
    int calOverdue = 0, calSoon = 0, maintOverdue = 0, outOfService = 0, underMaint = 0, quarantined = 0;
    for (var a : allAssets) {
      String cs = BiomedBoardRules.calibrationState(a.getCalibrationDue(), today);
      LocalDate npd = nextPlanDue.get(a.getId());
      String ms = BiomedBoardRules.maintenanceState(npd, today);
      boolean pInc = patientIncAssets.contains(a.getId());
      int rank = BiomedBoardRules.assetRank(a.getStatus(), cs, ms, pInc);
      if ("overdue".equals(cs)) calOverdue++;
      if ("due_soon".equals(cs)) calSoon++;
      if ("overdue".equals(ms)) maintOverdue++;
      if ("out_of_service".equals(a.getStatus())) outOfService++;
      if ("under_maintenance".equals(a.getStatus())) underMaint++;
      if ("quarantined".equals(a.getStatus())) quarantined++;
      if (rank == 9) continue;
      var row = new LinkedHashMap<String,Object>();
      row.put("id", a.getId()); row.put("asset_code", a.getAssetCode()); row.put("type", a.getType());
      row.put("manufacturer", a.getManufacturer()); row.put("model", a.getModel());
      row.put("serial_number", a.getSerialNumber()); row.put("location", a.getLocation());
      row.put("owner", a.getOwner()); row.put("status", a.getStatus());
      row.put("calibration_due", a.getCalibrationDue() == null ? null : a.getCalibrationDue().toString());
      row.put("calibration_state", cs);
      row.put("calibration_days", a.getCalibrationDue() == null ? null : (int) java.time.temporal.ChronoUnit.DAYS.between(today, a.getCalibrationDue()));
      row.put("next_maintenance_due", npd == null ? null : npd.toString());
      row.put("maintenance_state", ms);
      row.put("open_work_orders", openWoByAsset.getOrDefault(a.getId(), 0L));
      row.put("open_incidents", openIncByAsset.getOrDefault(a.getId(), 0L));
      row.put("patient_incident_open", pInc);
      row.put("usable", AssetRules.usable(a.getStatus(), a.getCalibrationDue(), today));
      row.put("rank", rank);
      queue.add(row);
    }
    queue.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("rank"))
        .thenComparing(r -> String.valueOf(r.get("asset_code"))));
    List<Map<String,Object>> woRows = new ArrayList<>();
    int awaitingRelease = 0; java.math.BigDecimal downtime = java.math.BigDecimal.ZERO;
    for (var w : allWos) {
      if (w.getDowntimeHours() != null && ("completed".equals(w.getStatus()) || "released".equals(w.getStatus())))
        downtime = downtime.add(w.getDowntimeHours());
      if (!"open".equals(w.getStatus()) && !"completed".equals(w.getStatus())) continue;
      if ("completed".equals(w.getStatus())) awaitingRelease++;
      var a = assets.findById(w.getAssetId()).orElse(null);
      var row = new LinkedHashMap<String,Object>();
      row.put("id", w.getId()); row.put("asset_id", w.getAssetId());
      row.put("asset_code", a == null ? null : a.getAssetCode());
      row.put("type", w.getType()); row.put("technician", w.getTechnician());
      row.put("status", w.getStatus()); row.put("spare_part_shortage", w.isSparePartShortage());
      row.put("spare_purchase_ref", w.getSparePurchaseRef());
      row.put("age_hours", w.getCreatedAt() == null ? null : (int) java.time.Duration.between(w.getCreatedAt(), OffsetDateTime.now()).toHours());
      woRows.add(row);
    }
    woRows.sort(Comparator.comparing((Map<String,Object> r) -> "open".equals(r.get("status")) ? 0 : 1)
        .thenComparing(r -> -((Number) r.getOrDefault("age_hours", 0)).intValue()));
    long incOpen = allIncs.stream().filter(i -> "open".equals(i.getStatus())).count();
    var summary = new LinkedHashMap<String,Object>();
    summary.put("assets_total", allAssets.size());
    summary.put("assets_in_service", allAssets.stream().filter(a -> "in_service".equals(a.getStatus())).count());
    summary.put("out_of_service", outOfService); summary.put("under_maintenance", underMaint);
    summary.put("quarantined", quarantined);
    summary.put("calibration_overdue", calOverdue); summary.put("calibration_due_soon", calSoon);
    summary.put("maintenance_overdue", maintOverdue);
    summary.put("work_orders_open", woRows.stream().filter(r -> "open".equals(r.get("status"))).count());
    summary.put("awaiting_release", awaitingRelease);
    summary.put("incidents_open", incOpen);
    summary.put("incidents_patient_affected", patientIncAssets.size());
    summary.put("recorded_downtime_hours", downtime);
    summary.put("attention_assets", queue.size());
    return Map.of("ok", true, "assets", queue, "work_orders", woRows, "summary", summary);
  }
  @GetMapping("/{id}/lifecycle")
  Map<String,Object> lifecycle(@PathVariable UUID id) {
    asset(id);
    return Map.of("ok",true,"events",events.findByAssetIdOrderByCreatedAtAsc(id));
  }
}
