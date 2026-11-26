package eg.gov.health.platform.transparencydeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Transparency & public disclosure (module 140).
 *  Publishable data managed under the law; source, update date
 *  and methodology shown; information requests and responses
 *  handled; published information corrected and the inaccurate
 *  withdrawn; sensitive data kept unpublished; and aggregated
 *  public data interfaces provided. */
@RestController @RequestMapping("/api/transparency-deep")
public class TransparencyDeepController {
  private final PublicationRepository publications; private final AuditService audit;
  public TransparencyDeepController(PublicationRepository publications, AuditService audit) {
    this.publications=publications; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private Publication publication(UUID id) {
    return publications.findById(id).orElseThrow(() -> BizException.notFound("publication_not_found","المنشور غير موجود"));
  }

  public record PubBody(String title, String sourceRef, String methodology, String legalBasis, Boolean containsSensitive) {}
  @PostMapping("/publications")
  Map<String,Object> create(@RequestBody PubBody r) {
    var p = principal();
    var pub = publications.save(new Publication(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.sourceRef(),
      r.methodology(), r.legalBasis(), Boolean.TRUE.equals(r.containsSensitive())));
    return Map.of("ok",true,"publication",pub);
  }
  @PostMapping("/publications/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal(); var pub = publication(id);
    var blockers = TransparencyRules.publishBlockers(pub.getSourceRef(), pub.getMethodology(), pub.getLegalBasis(), pub.isContainsSensitive());
    if (!blockers.isEmpty())
      throw BizException.conflict("not_publishable","نشر عام يقف على: " + String.join("، ", blockers) + " — الشفافية عن المنظومة ليست كشفًا لمرضاها");
    pub.publish(); publications.save(pub);
    return Map.of("ok",true,"publication",pub);
  }
  public record CorrectBody(String correctionNote, Boolean withdraw) {}
  @PostMapping("/publications/{id}/correct")
  Map<String,Object> correct(@PathVariable UUID id, @RequestBody CorrectBody r) {
    principal(); var pub = publication(id);
    if (!TransparencyRules.correctionValid(r.correctionNote()))
      throw BizException.badRequest("note_required","تصحيح منشور يحتاج ملاحظة ظاهرة — سجل عام يتغير بلا أثر يعلّم الجمهور ألا يثق بشيء ينشره");
    if (Boolean.TRUE.equals(r.withdraw())) pub.withdraw(r.correctionNote()); else pub.correct(r.correctionNote());
    publications.save(pub);
    audit.record("transparency.correction","publication",id.toString(),"{}");
    return Map.of("ok",true,"publication",pub);
  }
}
