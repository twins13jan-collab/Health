package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/documents")
public class DocumentsController {
  private final MedicalDocumentRepository repo; private final AuditService audit;
  public DocumentsController(MedicalDocumentRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record DocRequest(UUID patientId, String category, String title, LocalDate documentDate, String storageUri) {}
  @PostMapping
  Map<String,Object> add(@RequestBody DocRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.category()==null||r.title()==null||r.documentDate()==null) throw BizException.badRequest("missing_field","بيانات الوثيقة ناقصة");
    var d = repo.save(new MedicalDocument(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.category(), r.title(), r.documentDate(), r.storageUri()));
    audit.record("document.added","medical_document",d.getId().toString(),"{}");
    return Map.of("ok",true,"document",d,"retention_years",RetentionRules.retentionYears(r.category()),
      "disposal_eligible_from",RetentionRules.disposalEligibleFrom(r.documentDate(), r.category()));
  }
  @PostMapping("/{id}/dispose")
  Map<String,Object> dispose(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var d = repo.findById(id).orElseThrow(() -> BizException.notFound("document_not_found","الوثيقة غير موجودة"));
    boolean authorised = Boolean.parseBoolean(body.getOrDefault("authorised","false"));
    if (!RetentionRules.disposalAllowed(d.getDocumentDate(), d.getCategory(), LocalDate.now(), authorised))
      throw BizException.conflict("retention_not_met","الإتلاف ممنوع قبل انتهاء مدة الاحتفاظ أو بلا تصريح موثق");
    d.setStatus("disposed"); repo.save(d);
    audit.record("document.disposed","medical_document",id.toString(),"{}");
    return Map.of("ok",true,"document",d);
  }
  @GetMapping
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByDocumentDateDesc(patientId);
    return Map.of("ok",true,"documents",rows,"count",rows.size());
  }
}
