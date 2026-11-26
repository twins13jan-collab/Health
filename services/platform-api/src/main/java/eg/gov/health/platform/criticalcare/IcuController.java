package eg.gov.health.platform.criticalcare;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.PatientRepository;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** ICU & device monitoring (cycle 221). Hard rules from the catalog:
 *  stale device data is labelled, never shown as live; weight-based dose
 *  calculation is blocked until the reference weight is verified; alarm
 *  silence is capped; escalations record call and response times. */
@RestController @RequestMapping("/api/icu")
public class IcuController {
  private final IcuStayRepository stays; private final IcuObservationRepository obs;
  private final VentilationEpisodeRepository vents; private final IcuInfusionRepository infusions;
  private final EscalationEventRepository escalations; private final AuditService audit;
  private final PatientRepository patients;
  public IcuController(IcuStayRepository stays, IcuObservationRepository obs, VentilationEpisodeRepository vents,
                       IcuInfusionRepository infusions, EscalationEventRepository escalations, AuditService audit,
                       PatientRepository patients) {
    this.stays=stays; this.obs=obs; this.vents=vents; this.infusions=infusions; this.escalations=escalations; this.audit=audit;
    this.patients=patients;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private IcuStay stay(UUID id) { return stays.findById(id).orElseThrow(() -> BizException.notFound("icu_stay_not_found","إقامة العناية غير موجودة")); }

  public record AdmitRequest(UUID patientId, UUID encounterId, String admissionReason, String careLevel, String team,
                             String primaryDiagnosis, Double referenceWeight, String weightUnit, Boolean weightVerified) {}
  @PostMapping("/stays")
  Map<String,Object> admit(@RequestBody AdmitRequest r) {
    var p = principal();
    if (r.patientId()==null||r.admissionReason()==null) throw BizException.badRequest("invalid_admission","المريض وسبب القبول مطلوبان");
    Double kg = null;
    if (r.referenceWeight()!=null) kg = IcuRules.normalizedWeightKg(r.referenceWeight(), r.weightUnit()==null?"kg":r.weightUnit());
    var s = stays.save(new IcuStay(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(),
      r.admissionReason(), r.careLevel(), r.team(), r.primaryDiagnosis(), kg, r.weightUnit(),
      Boolean.TRUE.equals(r.weightVerified())));
    audit.record("icu.stay.admitted","icu_stay",s.getId().toString(),"{}");
    return Map.of("ok",true,"stay",s,"weight_kg",kg==null?"":kg,
      "dose_calculation_allowed", IcuRules.doseCalculationAllowed(s.isWeightVerified()));
  }

  public record ObservationRequest(String kind, Double value, String unit, String deviceCode, OffsetDateTime sourceTime) {}
  @PostMapping("/stays/{id}/observations")
  Map<String,Object> observe(@PathVariable UUID id, @RequestBody ObservationRequest r) {
    var p = principal(); var s = stay(id);
    if (!"active".equals(s.getStatus())) throw BizException.conflict("stay_closed","الإقامة منتهية — لا ملاحظات جديدة");
    if (r.kind()==null||r.value()==null||r.sourceTime()==null) throw BizException.badRequest("invalid_observation","النوع والقيمة ووقت المصدر مطلوبة — كل قراءة تحمل وقت مصدرها (دورة 221)");
    var quality = IcuRules.qualityFor(r.kind(), r.value());
    var o = obs.save(new IcuObservation(s.getFacilityId(), p.governorate(), id, r.kind(), r.value(), r.unit(),
      r.deviceCode(), r.sourceTime(), quality, p.userId()));
    var freshness = IcuRules.freshness(r.sourceTime(), OffsetDateTime.now());
    audit.record("icu.observation.recorded","icu_observation",o.getId().toString(),"{}");
    return Map.of("ok",true,"observation",o,"quality",quality,"freshness",freshness.name(),
      "present_as_live", IcuRules.mayPresentAsLive(freshness));
  }
  @GetMapping("/stays/{id}/observations")
  Map<String,Object> observations(@PathVariable UUID id) {
    stay(id); var rows = obs.findByStayIdOrderBySourceTimeDesc(id);
    var now = OffsetDateTime.now();
    var enriched = rows.stream().map(o -> {
      var f = IcuRules.freshness(o.getSourceTime(), now);
      return Map.<String,Object>of("id",o.getId(),"kind",o.getKind(),"value",o.getValue(),"quality",o.getQuality(),
        "source_time",o.getSourceTime(),"freshness",f.name(),"present_as_live",IcuRules.mayPresentAsLive(f));
    }).toList();
    return Map.of("ok",true,"observations",enriched,"count",rows.size());
  }

  public record VentRequest(String deviceCode, String mode, Double fio2, Double peep, Double tidalVolumeMl, Double ratePerMin) {}
  @PostMapping("/stays/{id}/ventilation")
  Map<String,Object> startVent(@PathVariable UUID id, @RequestBody VentRequest r) {
    var p = principal(); var s = stay(id);
    if (r.deviceCode()==null||r.mode()==null) throw BizException.badRequest("invalid_ventilation","الجهاز والنمط مطلوبان");
    if (r.fio2()!=null && (r.fio2() < 0.21 || r.fio2() > 1.0)) throw BizException.badRequest("invalid_fio2","FiO2 بين 0.21 و1.0");
    var v = vents.save(new VentilationEpisode(s.getFacilityId(), p.governorate(), id, r.deviceCode(), r.mode(),
      r.fio2(), r.peep(), r.tidalVolumeMl(), r.ratePerMin(), p.userId()));
    audit.record("icu.ventilation.started","ventilation_episode",v.getId().toString(),"{}");
    return Map.of("ok",true,"episode",v);
  }
  @PostMapping("/ventilation/{id}/end")
  Map<String,Object> endVent(@PathVariable UUID id) {
    var v = vents.findById(id).orElseThrow(() -> BizException.notFound("vent_not_found","حلقة التنفس غير موجودة"));
    if (!"active".equals(v.getStatus())) throw BizException.conflict("already_ended","الحلقة منتهية مسبقًا");
    v.end(); vents.save(v);
    audit.record("icu.ventilation.ended","ventilation_episode",id.toString(),"{}");
    return Map.of("ok",true,"episode",v);
  }

  public record InfusionRequest(String drugCode, String concentration, Double doseRate, String rateUnit) {}
  @PostMapping("/stays/{id}/infusions")
  Map<String,Object> startInfusion(@PathVariable UUID id, @RequestBody InfusionRequest r) {
    var p = principal(); var s = stay(id);
    if (r.drugCode()==null||r.doseRate()==null||r.rateUnit()==null) throw BizException.badRequest("invalid_infusion","الدواء والمعدل ووحدته مطلوبة");
    boolean weightBased = r.rateUnit().contains("/kg");
    Double totalRate = null;
    if (weightBased) {
      // Catalog 221: weight/unit discrepancy blocks automatic calculation
      // until the reference weight is verified by a clinician.
      if (!IcuRules.doseCalculationAllowed(s.isWeightVerified()) || s.getReferenceWeightKg()==null)
        throw BizException.conflict("weight_not_verified","الحساب الآلي بالوزن ممنوع قبل التحقق من الوزن المرجعي ووحدته (دورة 221)");
      totalRate = IcuRules.totalRate(r.doseRate(), s.getReferenceWeightKg());
    }
    var inf = infusions.save(new IcuInfusion(s.getFacilityId(), p.governorate(), id, r.drugCode(), r.concentration(),
      r.doseRate(), r.rateUnit(), s.getReferenceWeightKg(), p.userId()));
    audit.record("icu.infusion.started","icu_infusion",inf.getId().toString(),"{}");
    return Map.of("ok",true,"infusion",inf,"computed_total_rate_per_hour",totalRate==null?"":totalRate);
  }
  @PostMapping("/infusions/{id}/stop")
  Map<String,Object> stopInfusion(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var inf = infusions.findById(id).orElseThrow(() -> BizException.notFound("infusion_not_found","التسريب غير موجود"));
    String to = body.getOrDefault("status","stopped");
    if (!"running".equals(inf.getStatus()) || !(to.equals("stopped")||to.equals("completed")))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح للتسريب");
    inf.stop(to); infusions.save(inf);
    audit.record("icu.infusion."+to,"icu_infusion",id.toString(),"{}");
    return Map.of("ok",true,"infusion",inf);
  }

  public record EscalationRequest(String reason, String teamCalled) {}
  @PostMapping("/stays/{id}/escalations")
  Map<String,Object> escalate(@PathVariable UUID id, @RequestBody EscalationRequest r) {
    var p = principal(); var s = stay(id);
    if (r.reason()==null||r.teamCalled()==null) throw BizException.badRequest("invalid_escalation","سبب التصعيد والفريق المستدعى مطلوبان");
    var e = escalations.save(new EscalationEvent(s.getFacilityId(), p.governorate(), id, r.reason(), r.teamCalled()));
    audit.record("icu.escalation.called","escalation_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"escalation",e);
  }
  @PostMapping("/escalations/{id}/respond")
  Map<String,Object> respond(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var e = escalations.findById(id).orElseThrow(() -> BizException.notFound("escalation_not_found","التصعيد غير موجود"));
    if (!"open".equals(e.getStatus())) throw BizException.conflict("already_responded","تمت الاستجابة مسبقًا");
    String decision = body.get("decision");
    if (decision==null||decision.isBlank()) throw BizException.badRequest("decision_required","قرار الفريق المستدعى مطلوب");
    e.respond(decision); escalations.save(e);
    long minutes = Duration.between(e.getCalledAt(), OffsetDateTime.now()).toMinutes();
    audit.record("icu.escalation.responded","escalation_event",id.toString(),"{}");
    return Map.of("ok",true,"escalation",e,"response_minutes",minutes);
  }

  @PostMapping("/stays/{id}/status")
  Map<String,Object> endStay(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var s = stay(id);
    String to = body.getOrDefault("status","");
    if (!"active".equals(s.getStatus()) || !(to.equals("discharged")||to.equals("transferred")||to.equals("deceased")))
      throw BizException.conflict("illegal_transition","إنهاء غير مسموح: "+s.getStatus()+" -> "+to);
    s.endStay(to); stays.save(s);
    audit.record("icu.stay."+to,"icu_stay",id.toString(),"{}");
    return Map.of("ok",true,"stay",s);
  }
  /** Operational unit board (Phase 4.6): every active stay of the
   *  caller's facility as an attention queue — latest device vitals with
   *  their freshness (stale data labelled, never live), ventilation and
   *  running infusions, and open escalations first. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    var now = OffsetDateTime.now();
    var active = stays.findByFacilityIdAndStatus(UUID.fromString(p.facilityId()), "active");
    var rows = new ArrayList<Map<String,Object>>();
    int ventilated = 0, signalLost = 0, staleCount = 0, noData = 0, openEscTotal = 0, weightUnverified = 0;
    for (var s : active) {
      var obsRows = obs.findByStayIdOrderBySourceTimeDesc(s.getId());
      var latest = new LinkedHashMap<String, Map<String,Object>>();
      for (var o : obsRows) {
        if (latest.containsKey(o.getKind())) continue;
        var f = IcuRules.freshness(o.getSourceTime(), now);
        var vm = new LinkedHashMap<String,Object>();
        vm.put("value", o.getValue()); vm.put("quality", o.getQuality());
        vm.put("source_time", o.getSourceTime()); vm.put("freshness", f.name());
        vm.put("present_as_live", IcuRules.mayPresentAsLive(f));
        latest.put(o.getKind(), vm);
      }
      String signal = IcuBoardRules.signalOf(!obsRows.isEmpty(),
        obsRows.isEmpty() ? null : IcuRules.freshness(obsRows.get(0).getSourceTime(), now));
      var vent = vents.findByStayIdOrderByStartedAtDesc(s.getId()).stream()
        .filter(v -> "active".equals(v.getStatus())).findFirst().orElse(null);
      var running = infusions.findByStayIdOrderByStartedAtDesc(s.getId()).stream()
        .filter(i -> "running".equals(i.getStatus())).toList();
      var open = escalations.findByStayIdOrderByCalledAtDesc(s.getId()).stream()
        .filter(e -> "open".equals(e.getStatus())).toList();
      if (vent != null) ventilated++;
      if ("signal_lost".equals(signal)) signalLost++;
      if ("stale".equals(signal)) staleCount++;
      if ("no_data".equals(signal)) noData++;
      openEscTotal += open.size();
      if (!s.isWeightVerified()) weightUnverified++;
      var row = new LinkedHashMap<String,Object>();
      row.put("stay", s);
      row.put("patient_name", patients.findById(s.getPatientId()).map(pt -> pt.getFullName()).orElse(""));
      row.put("signal", signal);
      row.put("attention_rank", IcuBoardRules.attentionRank(!open.isEmpty(), signal));
      row.put("vitals", latest);
      row.put("ventilated", vent != null);
      row.put("vent_id", vent == null ? "" : vent.getId());
      row.put("vent_mode", vent == null ? "" : vent.getMode());
      row.put("open_escalation_list", open.stream().map(e -> Map.<String,Object>of(
        "id", e.getId(), "reason", e.getReason(), "team_called", e.getTeamCalled(), "called_at", e.getCalledAt())).toList());
      row.put("running_infusions", running.stream().map(i -> Map.<String,Object>of(
        "id", i.getId(), "drug_code", i.getDrugCode(), "dose_rate", i.getDoseRate(), "rate_unit", i.getRateUnit())).toList());
      row.put("open_escalations", open.size());
      row.put("latest_escalation_reason", open.isEmpty() ? "" : open.get(0).getReason());
      rows.add(row);
    }
    rows.sort(Comparator.<Map<String,Object>>comparingInt(r -> (Integer) r.get("attention_rank"))
      .thenComparing(r -> ((IcuStay) r.get("stay")).getAdmittedAt()));
    var summary = new LinkedHashMap<String,Object>();
    summary.put("active_stays", active.size()); summary.put("ventilated", ventilated);
    summary.put("signal_lost", signalLost); summary.put("stale", staleCount); summary.put("no_data", noData);
    summary.put("open_escalations", openEscTotal); summary.put("weight_unverified", weightUnverified);
    return Map.of("ok", true, "board", rows, "count", rows.size(), "summary", summary, "generated_at", now);
  }

  @GetMapping("/stays")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = stays.findByPatientIdOrderByAdmittedAtDesc(patientId);
    return Map.of("ok",true,"stays",rows,"count",rows.size());
  }
}
