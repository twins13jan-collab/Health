package eg.gov.health.platform.pharmacy;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.clinical.Allergy;
import eg.gov.health.platform.clinical.AllergyRepository;
import eg.gov.health.platform.clinical.AllergyRules;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/pharmacy")
public class PharmacyController {
  private final PrescriptionRepository repo; private final AuditService audit; private final AllergyRepository allergyRepo;
  public PharmacyController(PrescriptionRepository repo, AuditService audit, AllergyRepository allergyRepo) { this.repo=repo; this.audit=audit; this.allergyRepo=allergyRepo; }
  public record RxRequest(UUID patientId, UUID encounterId, String drugCode, String drugName, String dose, String frequency, String duration) {}
  @PostMapping("/prescriptions")
  Map<String,Object> prescribe(@RequestBody RxRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.drugCode()==null) throw BizException.badRequest("missing_field","patientId و drugCode مطلوبان");
    // Allergy HARD STOP (clinical deepening): same drug / class / cross-reactive
    // class against an ACTIVE allergy blocks prescribing outright.
    var allergyConflict = AllergyRules.conflict(r.drugCode(),
      allergyRepo.findByPatientIdAndStatus(r.patientId(), "active").stream().map(Allergy::toView).toList());
    if (allergyConflict.isPresent())
      throw BizException.conflict("allergy_hard_stop","حساسية موثقة تمنع هذا الدواء: "+allergyConflict.get()+" — التجاوز بقرار استشاري موثق فقط");
    var active = repo.findByPatientIdAndStatusIn(r.patientId(), List.of("prescribed","verified","dispensed"));
    var activeCodes = active.stream().map(Prescription::getDrugCode).toList();
    var interaction = InteractionRules.severeInteraction(r.drugCode(), activeCodes);
    boolean duplicate = InteractionRules.isDuplicate(r.drugCode(), activeCodes);
    var rx = repo.save(new Prescription(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(),
      r.drugCode(), r.drugName(), r.dose(), r.frequency(), r.duration(), p.userId()));
    audit.record("pharmacy.prescribed","prescription",rx.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("prescription",rx);
    if (interaction.isPresent()) out.put("warning", Map.of("type","severe_interaction","pair",interaction.get(),
      "action","يتطلب تحقق الصيدلي السريري مع توثيق السبب قبل الصرف (دورة 222)"));
    if (duplicate) out.put("duplicate_warning","نفس الدواء فعال بالفعل لهذا المريض — راجع المصالحة الدوائية");
    return out;
  }
  @PostMapping("/prescriptions/{id}/dispense")
  Map<String,Object> dispense(@PathVariable UUID id, @RequestBody(required=false) Map<String,String> body) {
    var rx = repo.findById(id).orElseThrow(() -> BizException.notFound("rx_not_found","الوصفة غير موجودة"));
    if ("dispensed".equals(rx.getStatus())) throw BizException.conflict("already_dispensed","صُرفت مسبقًا — منع الازدواج (دورة 222)");
    if ("cancelled".equals(rx.getStatus())) throw BizException.conflict("cancelled_rx","وصفة ملغاة لا تُصرف");
    rx.setStatus("dispensed"); repo.save(rx);
    audit.record("pharmacy.dispensed","prescription",id.toString(),"{}");
    return Map.of("ok",true,"prescription",rx);
  }
  @GetMapping("/prescriptions")
  Map<String,Object> list(@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Paging.page(rows, "prescriptions", page, size);
  }
}
