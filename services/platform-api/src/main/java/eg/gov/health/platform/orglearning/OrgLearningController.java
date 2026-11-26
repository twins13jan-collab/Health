package eg.gov.health.platform.orglearning;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Organizational learning & procedural knowledge (module 80).
 *  A unified library of policies, procedures, operating guides
 *  and clinical and administrative instructions; every document
 *  bound to its owner, version, effective date, review date and
 *  beneficiaries; acknowledgment of reading, mandatory training
 *  and comprehension of changes managed; lessons from incidents,
 *  crises, audits and complaints captured; and unified search
 *  respecting access permissions and usage rights. */
@RestController @RequestMapping("/api/org-learning")
public class OrgLearningController {
  private final KnowledgeDocumentRepository documents; private final ReadAcknowledgmentRepository acks;
  private final LessonLearnedRepository lessons; private final AuditService audit;
  public OrgLearningController(KnowledgeDocumentRepository documents, ReadAcknowledgmentRepository acks,
                               LessonLearnedRepository lessons, AuditService audit) {
    this.documents=documents; this.acks=acks; this.lessons=lessons; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private KnowledgeDocument document(UUID id) {
    return documents.findById(id).orElseThrow(() -> BizException.notFound("document_not_found","الوثيقة غير موجودة"));
  }
  private LessonLearned lesson(UUID id) {
    return lessons.findById(id).orElseThrow(() -> BizException.notFound("lesson_not_found","الدرس غير موجود"));
  }

  public record DocBody(String title, String ownerRef, String version, String audience, LocalDate effectiveFrom, LocalDate reviewDue, Boolean mandatory) {}
  @PostMapping("/documents")
  Map<String,Object> create(@RequestBody DocBody r) {
    var p = principal();
    var d = documents.save(new KnowledgeDocument(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.ownerRef(), r.version(),
      r.audience(), r.effectiveFrom(), r.reviewDue(), Boolean.TRUE.equals(r.mandatory())));
    return Map.of("ok",true,"document",d,"current",LearningRules.current(r.effectiveFrom(), r.reviewDue(), LocalDate.now()));
  }
  @GetMapping("/documents/{id}/currency")
  Map<String,Object> currency(@PathVariable UUID id) {
    principal(); var d = document(id);
    boolean cur = LearningRules.current(d.getEffectiveFrom(), d.getReviewDue(), LocalDate.now());
    return Map.of("ok",true,"document_id",id.toString(),"current",cur,
      "note", cur ? "in force" : "not current — outside its window or past review: an expired-review procedure on the shelf is a hazard");
  }
  public record AckBody(String userRef, Boolean acknowledged, Boolean comprehensionAssessed) {}
  @PostMapping("/documents/{id}/acknowledgments")
  Map<String,Object> ack(@PathVariable UUID id, @RequestBody AckBody r) {
    var p = principal(); document(id);
    var a = acks.save(new ReadAcknowledgment(UUID.fromString(p.facilityId()), p.governorate(), id, r.userRef(),
      Boolean.TRUE.equals(r.acknowledged()), Boolean.TRUE.equals(r.comprehensionAssessed())));
    boolean understood = LearningRules.understood(a.isAcknowledged(), a.isComprehensionAssessed());
    return Map.of("ok",true,"acknowledgment",a,"counts_as_understood",understood,
      "note","reading is not understanding — acknowledgment counts only with a comprehension assessment");
  }
  public record LessonBody(String sourceType, String sourceRef, String lesson, Boolean applied, String appliedIn) {}
  @PostMapping("/lessons")
  Map<String,Object> lesson(@RequestBody LessonBody r) {
    var p = principal();
    var l = lessons.save(new LessonLearned(UUID.fromString(p.facilityId()), p.governorate(), r.sourceType(), r.sourceRef(), r.lesson(),
      Boolean.TRUE.equals(r.applied()), r.appliedIn()));
    return Map.of("ok",true,"lesson",l);
  }
  @PostMapping("/lessons/{id}/close")
  Map<String,Object> closeLesson(@PathVariable UUID id) {
    principal(); var l = lesson(id);
    if (!LearningRules.lessonClosable(l.isApplied(), l.getAppliedIn()))
      throw BizException.conflict("lesson_not_applied","درس مستفاد لا يُغلق قبل تطبيقه في وثيقة حية — درس يُحفظ يُعاد ارتكابه");
    l.close(); lessons.save(l);
    audit.record("learning.lesson.applied","lesson_learned",id.toString(),"{}");
    return Map.of("ok",true,"lesson",l);
  }
}
