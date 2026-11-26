package eg.gov.health.platform.adt;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.PatientRepository;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
/** ADT — admission / discharge / transfer (Phase 4, hospital operations).
 *  The facility's live census: wards with bed-level occupancy, admissions
 *  that cannot double-book a bed, internal transfers with reasons, and
 *  discharges that require a disposition. Every write is audited. */
@RestController @RequestMapping("/api/adt")
public class AdtController {
  private final WardRepository wards; private final InpatientAdmissionRepository admissions;
  private final WardTransferRepository transfers; private final PatientRepository patients;
  private final AuditService audit;
  public AdtController(WardRepository wards, InpatientAdmissionRepository admissions,
                       WardTransferRepository transfers, PatientRepository patients, AuditService audit) {
    this.wards=wards; this.admissions=admissions; this.transfers=transfers; this.patients=patients; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private UUID facilityId() { return UUID.fromString(principal().facilityId()); }
  private Ward ward(UUID id) {
    var w = wards.findById(id).orElseThrow(() -> BizException.notFound("ward_not_found","الجناح غير موجود"));
    if (!w.getFacilityId().equals(facilityId())) throw BizException.notFound("ward_not_found","الجناح غير موجود");
    return w;
  }
  private InpatientAdmission admission(UUID id) {
    var a = admissions.findById(id).orElseThrow(() -> BizException.notFound("admission_not_found","الإقامة غير موجودة"));
    if (!a.getFacilityId().equals(facilityId())) throw BizException.notFound("admission_not_found","الإقامة غير موجودة");
    return a;
  }
  private Map<String,Object> wardView(Ward w) {
    var active = admissions.findByWardIdAndStatus(w.getId(), "admitted");
    var m = new LinkedHashMap<String,Object>();
    m.put("id", w.getId()); m.put("nameAr", w.getNameAr()); m.put("nameEn", w.getNameEn());
    m.put("wardType", w.getWardType()); m.put("totalBeds", w.getTotalBeds());
    m.put("occupied", active.size()); m.put("free", Math.max(0, w.getTotalBeds()-active.size()));
    m.put("occupancyPct", w.getTotalBeds()==0 ? null : Math.round(1000.0*active.size()/w.getTotalBeds())/10.0);
    m.put("active", w.isActive());
    return m;
  }
  private Map<String,Object> admissionView(InpatientAdmission a) {
    var m = new LinkedHashMap<String,Object>();
    m.put("id", a.getId()); m.put("patientId", a.getPatientId()); m.put("encounterId", a.getEncounterId());
    patients.findById(a.getPatientId()).ifPresent(pt -> { m.put("patientName", pt.getFullName()); m.put("nationalId", pt.getNationalId()); });
    var w = wards.findById(a.getWardId()).orElse(null);
    m.put("wardId", a.getWardId()); m.put("wardName", w==null?null:w.getNameAr());
    m.put("bedLabel", a.getBedLabel()); m.put("attendingName", a.getAttendingName());
    m.put("admissionReason", a.getAdmissionReason()); m.put("source", a.getSource()); m.put("status", a.getStatus());
    m.put("admittedAt", a.getAdmittedAt()); m.put("dischargedAt", a.getDischargedAt());
    m.put("dischargeDisposition", a.getDischargeDisposition());
    m.put("losDays", AdtRules.lengthOfStayDays(a.getAdmittedAt(), a.getDischargedAt()==null?OffsetDateTime.now():a.getDischargedAt()));
    return m;
  }

  @GetMapping("/wards")
  Map<String,Object> listWards() {
    var rows = wards.findByFacilityIdOrderByNameArAsc(facilityId()).stream().map(this::wardView).toList();
    return Map.of("ok",true,"wards",rows,"count",rows.size());
  }
  public record WardBody(String nameAr, String nameEn, String wardType, Integer totalBeds) {}
  @PostMapping("/wards")
  Map<String,Object> createWard(@RequestBody WardBody r) {
    var p = principal();
    if (r.nameAr()==null||r.nameAr().isBlank()) throw BizException.badRequest("ward_name_required","اسم الجناح مطلوب");
    var w = wards.save(new Ward(facilityId(), p.governorate(), r.nameAr(), r.nameEn(), r.wardType(), r.totalBeds()==null?0:r.totalBeds()));
    audit.record("adt.ward_created","ward",String.valueOf(w.getId()),"{\"beds\":"+w.getTotalBeds()+"}");
    return Map.of("ok",true,"ward",wardView(w));
  }
  @PostMapping("/wards/{id}/deactivate")
  Map<String,Object> deactivateWard(@PathVariable UUID id) {
    var w = ward(id);
    long active = admissions.findByWardIdAndStatus(id, "admitted").size();
    if (active>0) throw BizException.conflict("ward_has_patients","لا يمكن إيقاف جناح به مرضى مقيمون");
    w.deactivate(); wards.save(w);
    audit.record("adt.ward_deactivated","ward",String.valueOf(id),"{}");
    return Map.of("ok",true,"ward",wardView(w));
  }

  @GetMapping("/board")
  Map<String,Object> board() {
    var fid = facilityId();
    var wardRows = wards.findByFacilityIdAndActiveTrueOrderByNameArAsc(fid).stream().map(w -> {
      var m = wardView(w);
      var beds = admissions.findByWardIdAndStatus(w.getId(), "admitted").stream().map(a -> {
        var b = new LinkedHashMap<String,Object>();
        b.put("bedLabel", a.getBedLabel()); b.put("admissionId", a.getId());
        patients.findById(a.getPatientId()).ifPresent(pt -> b.put("patientName", pt.getFullName()));
        b.put("admittedAt", a.getAdmittedAt());
        return b;
      }).toList();
      m.put("beds", beds);
      return m;
    }).toList();
    long census = admissions.countByFacilityIdAndStatus(fid, "admitted");
    return Map.of("ok",true,"wards",wardRows,"census",census);
  }

  @GetMapping("/admissions")
  Map<String,Object> listAdmissions(@RequestParam(required=false, defaultValue="admitted") String status) {
    var rows = admissions.findByFacilityIdAndStatusOrderByAdmittedAtAsc(facilityId(), status).stream().map(this::admissionView).toList();
    return Map.of("ok",true,"admissions",rows,"count",rows.size());
  }
  public record AdmitBody(String patientId, String encounterId, String wardId, String bedLabel,
                          String attendingName, String admissionReason, String source) {}
  @PostMapping("/admissions")
  Map<String,Object> admit(@RequestBody AdmitBody r) {
    var p = principal();
    if (r.patientId()==null) throw BizException.badRequest("patient_required","المريض مطلوب");
    if (!AdtRules.validSource(r.source())) throw BizException.badRequest("invalid_source","مصدر قبول غير معروف");
    var patient = patients.findById(UUID.fromString(r.patientId()))
      .orElseThrow(() -> BizException.notFound("patient_not_found","المريض غير موجود في السجل الموحد"));
    var w = ward(UUID.fromString(r.wardId()));
    if (!w.isActive()) throw BizException.conflict("ward_inactive","الجناح موقوف");
    var active = admissions.findByWardIdAndStatus(w.getId(), "admitted");
    if (!AdtRules.wardHasCapacity(w.getTotalBeds(), active.size()))
      throw BizException.conflict("ward_full","الجناح ممتلئ — لا أسرّة شاغرة");
    var occupied = active.stream().map(InpatientAdmission::getBedLabel).toList();
    if (!AdtRules.bedFree(occupied, r.bedLabel()))
      throw BizException.conflict("bed_occupied","السرير مشغول بمريض آخر");
    var a = admissions.save(new InpatientAdmission(facilityId(), p.governorate(), patient.getId(),
      r.encounterId()==null?null:UUID.fromString(r.encounterId()), w.getId(), r.bedLabel().trim(),
      r.attendingName(), r.admissionReason(), r.source()));
    audit.record("adt.admit","inpatient_admission",String.valueOf(a.getId()),
      "{\"ward\":\""+w.getNameAr()+"\",\"bed\":\""+a.getBedLabel()+"\"}");
    return Map.of("ok",true,"admission",admissionView(a));
  }
  public record TransferBody(String toWardId, String toBedLabel, String reason) {}
  @PostMapping("/admissions/{id}/transfer")
  Map<String,Object> transfer(@PathVariable UUID id, @RequestBody TransferBody r) {
    var p = principal(); var a = admission(id);
    if (!AdtRules.dischargeable(a.getStatus())) throw BizException.conflict("not_admitted","الإقامة منتهية");
    var toWard = ward(UUID.fromString(r.toWardId()));
    var problem = AdtRules.transferProblem(a.getWardId(), a.getBedLabel(), toWard.getId(), r.toBedLabel(), r.reason());
    if (problem!=null) throw BizException.badRequest(problem,"بيانات التحويل غير مكتملة أو غير منطقية");
    var targetActive = admissions.findByWardIdAndStatus(toWard.getId(), "admitted");
    boolean sameWard = toWard.getId().equals(a.getWardId());
    if (!sameWard && !AdtRules.wardHasCapacity(toWard.getTotalBeds(), targetActive.size()))
      throw BizException.conflict("ward_full","الجناح المستهدف ممتلئ");
    var occupied = targetActive.stream().filter(x -> !x.getId().equals(a.getId())).map(InpatientAdmission::getBedLabel).toList();
    if (!AdtRules.bedFree(occupied, r.toBedLabel()))
      throw BizException.conflict("bed_occupied","السرير المستهدف مشغول");
    var tr = transfers.save(new WardTransfer(facilityId(), p.governorate(), a.getId(), a.getWardId(), toWard.getId(),
      a.getBedLabel(), r.toBedLabel().trim(), r.reason(), p.email()));
    a.moveTo(toWard.getId(), r.toBedLabel().trim()); admissions.save(a);
    audit.record("adt.transfer","inpatient_admission",String.valueOf(a.getId()),
      "{\"toWard\":\""+toWard.getNameAr()+"\",\"toBed\":\""+a.getBedLabel()+"\"}");
    return Map.of("ok",true,"admission",admissionView(a),"transferId",tr.getId());
  }
  public record DischargeBody(String disposition) {}
  @PostMapping("/admissions/{id}/discharge")
  Map<String,Object> discharge(@PathVariable UUID id, @RequestBody DischargeBody r) {
    var a = admission(id);
    if (!AdtRules.dischargeable(a.getStatus())) throw BizException.conflict("already_discharged","الإقامة منتهية أصلًا");
    if (!AdtRules.validDisposition(r.disposition()))
      throw BizException.badRequest("invalid_disposition","قرار خروج غير معروف — اختر: منزل/تحويل خارجي/وفاة/ضد النصح الطبي/تأهيل");
    a.discharge(r.disposition()); admissions.save(a);
    audit.record("adt.discharge","inpatient_admission",String.valueOf(a.getId()),
      "{\"disposition\":\""+r.disposition()+"\"}");
    return Map.of("ok",true,"admission",admissionView(a));
  }
  @GetMapping("/admissions/{id}/transfers")
  Map<String,Object> history(@PathVariable UUID id) {
    admission(id);
    var rows = transfers.findByAdmissionIdOrderByCreatedAtAsc(id);
    return Map.of("ok",true,"transfers",rows,"count",rows.size());
  }
}
