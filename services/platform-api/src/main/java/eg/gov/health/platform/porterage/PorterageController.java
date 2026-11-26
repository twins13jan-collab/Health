package eg.gov.health.platform.porterage;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Internal patient transport & hotel services (module 109).
 *  Transports are prioritized: a critical case — or any patient on
 *  oxygen — never moves without a named clinical escort. Response
 *  time is measured per priority against its target and breaches
 *  are flagged. Room turnover after discharge ends only when
 *  cleaning is done, the bed is ready and an inspector has signed.
 *  Laundry losses are counted openly, and patient service requests
 *  (meals, blankets, maintenance) are tracked to fulfilment. */
@RestController @RequestMapping("/api/porterage")
public class PorterageController {
  private final TransportRequestRepository transports; private final RoomTurnoverTaskRepository rooms;
  private final LaundryBatchRepository laundry; private final PatientServiceRequestRepository services; private final AuditService audit;
  public PorterageController(TransportRequestRepository transports, RoomTurnoverTaskRepository rooms,
                             LaundryBatchRepository laundry, PatientServiceRequestRepository services, AuditService audit) {
    this.transports=transports; this.rooms=rooms; this.laundry=laundry; this.services=services; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TransportRequest transport(UUID id) {
    return transports.findById(id).orElseThrow(() -> BizException.notFound("transport_not_found","طلب النقل غير موجود"));
  }

  public record TransportRequestDto(String patientRef, String fromLocation, String toLocation, String priority,
                                    Boolean escortRequired, Boolean oxygenRequired, OffsetDateTime requestedAt) {}
  @PostMapping("/transports")
  Map<String,Object> request(@RequestBody TransportRequestDto r) {
    var p = principal();
    var t = transports.save(new TransportRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.fromLocation(),
      r.toLocation(), r.priority(), Boolean.TRUE.equals(r.escortRequired()), Boolean.TRUE.equals(r.oxygenRequired()), r.requestedAt()));
    audit.record("porterage.transport.requested","transport_request",t.getId().toString(),"{}");
    return Map.of("ok",true,"transport",t,"escort_mandatory","critical".equals(r.priority()) || Boolean.TRUE.equals(r.oxygenRequired()));
  }
  public record AssignRequest(String porter, String escortName) {}
  @PostMapping("/transports/{id}/assign")
  Map<String,Object> assign(@PathVariable UUID id, @RequestBody AssignRequest r) {
    principal(); var t = transport(id);
    if (!PorterageRules.escortSatisfied(t.getPriority(), t.isOxygenRequired(), r.escortName()))
      throw BizException.conflict("escort_required","النقل الحرج أو المعتمد على الأكسجين لا يتحرك دون مرافق سريري مسمى");
    t.assign(r.porter(), r.escortName()); transports.save(t);
    audit.record("porterage.transport.assigned","transport_request",id.toString(),"{}");
    return Map.of("ok",true,"transport",t);
  }
  @PostMapping("/transports/{id}/pickup")
  Map<String,Object> pickup(@PathVariable UUID id) {
    principal(); var t = transport(id);
    if (!"assigned".equals(t.getStatus())) throw BizException.conflict("not_assigned","النقل يُستلم بعد الإسناد");
    t.pickup(); transports.save(t);
    return Map.of("ok",true,"transport",t);
  }
  public record DeliverRequest(OffsetDateTime deliveredAt) {}
  @PostMapping("/transports/{id}/deliver")
  Map<String,Object> deliver(@PathVariable UUID id, @RequestBody DeliverRequest r) {
    principal(); var t = transport(id);
    t.deliver(r.deliveredAt()); transports.save(t);
    audit.record("porterage.transport.completed","transport_request",id.toString(),"{}");
    return Map.of("ok",true,"transport",t,
      "response_minutes",PorterageRules.responseMinutes(t.getRequestedAt(), t.getDeliveredAt()),
      "target_minutes",PorterageRules.TARGET_MIN.getOrDefault(t.getPriority(), 45),
      "response_breached",PorterageRules.responseBreached(t.getPriority(), t.getRequestedAt(), t.getDeliveredAt()));
  }
  public record RoomRequest(String roomRef, OffsetDateTime dischargedAt) {}
  @PostMapping("/rooms")
  Map<String,Object> room(@RequestBody RoomRequest r) {
    var p = principal();
    var t = rooms.save(new RoomTurnoverTask(UUID.fromString(p.facilityId()), p.governorate(), r.roomRef(), r.dischargedAt()));
    return Map.of("ok",true,"turnover",t);
  }
  public record RoomProgressRequest(Boolean cleaningDone, Boolean bedReady, String inspectedBy) {}
  @PostMapping("/rooms/{id}/progress")
  Map<String,Object> roomProgress(@PathVariable UUID id, @RequestBody RoomProgressRequest r) {
    principal();
    var t = rooms.findById(id).orElseThrow(() -> BizException.notFound("room_not_found","مهمة الغرفة غير موجودة"));
    t.progress(Boolean.TRUE.equals(r.cleaningDone()), Boolean.TRUE.equals(r.bedReady()), r.inspectedBy()); rooms.save(t);
    audit.record("porterage.room.progress","room_turnover_task",id.toString(),"{}");
    return Map.of("ok",true,"turnover",t,"room_ready","ready".equals(t.getStatus()));
  }
  public record LaundryRequest(String batchRef, String batchType, Integer sentCount) {}
  @PostMapping("/laundry")
  Map<String,Object> laundrySend(@RequestBody LaundryRequest r) {
    var p = principal();
    var b = laundry.save(new LaundryBatch(UUID.fromString(p.facilityId()), p.governorate(), r.batchRef(), r.batchType(), r.sentCount()));
    return Map.of("ok",true,"batch",b);
  }
  public record LaundryReceiveRequest(Integer returnedCount) {}
  @PostMapping("/laundry/{id}/receive")
  Map<String,Object> laundryReceive(@PathVariable UUID id, @RequestBody LaundryReceiveRequest r) {
    principal();
    var b = laundry.findById(id).orElseThrow(() -> BizException.notFound("laundry_not_found","دفعة الغسيل غير موجودة"));
    if (r.returnedCount() != null && b.getSentCount() != null && r.returnedCount() > b.getSentCount())
      throw BizException.badRequest("laundry_overreturn","المُستلم لا يمكن أن يتجاوز المُرسل");
    b.receive(r.returnedCount() == null ? 0 : r.returnedCount()); laundry.save(b);
    int loss = PorterageRules.laundryLoss(b.getSentCount() == null ? 0 : b.getSentCount(), r.returnedCount());
    audit.record("porterage.laundry.received","laundry_batch",id.toString(),"{}");
    return Map.of("ok",true,"batch",b,"loss_count",loss,"loss_flagged",loss > 0);
  }
  public record ServiceRequestDto(String patientRef, String requestType, String detail) {}
  @PostMapping("/service-requests")
  Map<String,Object> service(@RequestBody ServiceRequestDto r) {
    var p = principal();
    var s = services.save(new PatientServiceRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.requestType(), r.detail()));
    return Map.of("ok",true,"service_request",s);
  }
  @PostMapping("/service-requests/{id}/fulfill")
  Map<String,Object> fulfill(@PathVariable UUID id) {
    principal();
    var s = services.findById(id).orElseThrow(() -> BizException.notFound("service_not_found","طلب الخدمة غير موجود"));
    s.fulfill(); services.save(s);
    return Map.of("ok",true,"service_request",s);
  }

  /** Support-services operations board: the transport queue in
   *  attention order (target breaches first, then critical, then
   *  mid-journey), room turnovers with their overdue flag, laundry
   *  batches with losses named, and open patient service requests.
   *  This is the screen the old generic page never had — its list
   *  endpoint did not exist. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    var fid = UUID.fromString(p.facilityId());
    var now = OffsetDateTime.now();
    var zone = ZoneId.of("Africa/Cairo");
    var todayStart = LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();

    var transportRows = new ArrayList<Map<String,Object>>();
    int tOpen = 0, tBreached = 0, tCritical = 0, deliveredToday = 0, breachedToday = 0;
    var allTransports = transports.findByFacilityIdOrderByRequestedAtAsc(fid);
    for (var t : allTransports) {
      boolean open = SupportBoardRules.isOpen(t.getStatus());
      boolean breached = open && SupportBoardRules.waitBreached(t.getPriority(), t.getRequestedAt(), now);
      if (open) { tOpen++; if (breached) tBreached++; if ("critical".equals(t.getPriority())) tCritical++; }
      if ("delivered".equals(t.getStatus()) && t.getDeliveredAt() != null && !t.getDeliveredAt().isBefore(todayStart)) {
        deliveredToday++;
        if (PorterageRules.responseBreached(t.getPriority(), t.getRequestedAt(), t.getDeliveredAt())) breachedToday++;
      }
      if (!open) continue;
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", t.getId()); m.put("patient_ref", t.getPatientRef());
      m.put("from_location", t.getFromLocation()); m.put("to_location", t.getToLocation());
      m.put("priority", t.getPriority()); m.put("status", t.getStatus());
      m.put("escort_required", t.isEscortRequired()); m.put("oxygen_required", t.isOxygenRequired());
      m.put("escort_name", t.getEscortName()); m.put("assigned_porter", t.getAssignedPorter());
      m.put("requested_at", t.getRequestedAt());
      m.put("wait_minutes", SupportBoardRules.waitMinutes(t.getRequestedAt(), now));
      m.put("target_minutes", PorterageRules.TARGET_MIN.getOrDefault(t.getPriority() == null ? "routine" : t.getPriority(), 45));
      m.put("breached", breached);
      m.put("rank", SupportBoardRules.transportRank(t.getStatus(), t.getPriority(), breached));
      transportRows.add(m);
    }
    transportRows.sort(Comparator.comparingInt((Map<String,Object> m) -> (Integer) m.get("rank"))
        .thenComparing(m -> String.valueOf(m.get("requested_at"))));

    var roomRows = new ArrayList<Map<String,Object>>();
    int roomsPending = 0, roomsOverdue = 0, roomsReadyToday = 0;
    for (var r : rooms.findByFacilityIdOrderByDischargedAtAsc(fid)) {
      boolean overdue = SupportBoardRules.roomOverdue(r.getStatus(), r.getDischargedAt(), now);
      if (!"ready".equals(r.getStatus())) { roomsPending++; if (overdue) roomsOverdue++; }
      if ("ready".equals(r.getStatus()) && r.getReadyAt() != null && !r.getReadyAt().isBefore(todayStart)) roomsReadyToday++;
      if ("ready".equals(r.getStatus())) continue;
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", r.getId()); m.put("room_ref", r.getRoomRef()); m.put("status", r.getStatus());
      m.put("discharged_at", r.getDischargedAt());
      m.put("cleaning_done", r.isCleaningDone()); m.put("bed_ready", r.isBedReady());
      m.put("inspected_by", r.getInspectedBy());
      m.put("wait_minutes", SupportBoardRules.waitMinutes(r.getDischargedAt(), now));
      m.put("overdue", overdue);
      roomRows.add(m);
    }

    var laundryRows = new ArrayList<Map<String,Object>>();
    int laundryOpen = 0, laundryLossToday = 0;
    for (var b : laundry.findByFacilityIdOrderByCreatedAtDesc(fid)) {
      String state = SupportBoardRules.laundryState(b.getStatus(), b.getSentCount(), b.getReturnedCount());
      int loss = PorterageRules.laundryLoss(b.getSentCount() == null ? 0 : b.getSentCount(), b.getReturnedCount());
      if ("in_process".equals(state)) laundryOpen++;
      if (loss > 0 && b.getUpdatedAt() != null && !b.getUpdatedAt().isBefore(todayStart)) laundryLossToday += loss;
      if (!"in_process".equals(state) && !(b.getCreatedAt() != null && !b.getCreatedAt().isBefore(todayStart))) continue;
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", b.getId()); m.put("batch_ref", b.getBatchRef()); m.put("batch_type", b.getBatchType());
      m.put("sent_count", b.getSentCount()); m.put("returned_count", b.getReturnedCount());
      m.put("loss_count", loss); m.put("state", state); m.put("created_at", b.getCreatedAt());
      laundryRows.add(m);
    }

    var serviceRows = new ArrayList<Map<String,Object>>();
    int serviceOpen = 0, serviceFulfilledToday = 0;
    for (var s : services.findByFacilityIdOrderByCreatedAtAsc(fid)) {
      if ("open".equals(s.getStatus())) {
        serviceOpen++;
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("patient_ref", s.getPatientRef());
        m.put("request_type", s.getRequestType()); m.put("detail", s.getDetail());
        m.put("created_at", s.getCreatedAt());
        m.put("wait_minutes", SupportBoardRules.waitMinutes(s.getCreatedAt(), now));
        serviceRows.add(m);
      }
      if ("fulfilled".equals(s.getStatus()) && s.getFulfilledAt() != null && !s.getFulfilledAt().isBefore(todayStart)) serviceFulfilledToday++;
    }

    Map<String,Object> summary = new LinkedHashMap<>();
    summary.put("transports_open", tOpen); summary.put("transports_breached", tBreached);
    summary.put("transports_critical_open", tCritical); summary.put("delivered_today", deliveredToday);
    summary.put("breached_today", breachedToday);
    summary.put("rooms_pending", roomsPending); summary.put("rooms_overdue", roomsOverdue);
    summary.put("rooms_ready_today", roomsReadyToday);
    summary.put("laundry_open", laundryOpen); summary.put("laundry_loss_today", laundryLossToday);
    summary.put("service_open", serviceOpen); summary.put("service_fulfilled_today", serviceFulfilledToday);
    return Map.of("ok", true, "transports", transportRows, "rooms", roomRows,
        "laundry", laundryRows, "service_requests", serviceRows, "summary", summary);
  }
}
