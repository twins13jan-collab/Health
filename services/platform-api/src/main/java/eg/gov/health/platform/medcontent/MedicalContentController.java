package eg.gov.health.platform.medcontent;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Medical content & health education (module 120). A medically
 *  reviewed, approved education library: diseases, medications,
 *  preparation and home care, in the patient's language with
 *  accessible formats. Publication requires clinical approval by a
 *  reviewer who is not the author, with source and references
 *  documented and a review date — and reviews expire: stale content
 *  is flagged and withdrawn, never quietly kept live. Awareness
 *  campaigns measure comprehension, not just reach. */
@RestController @RequestMapping("/api/medical-content")
public class MedicalContentController {
  private final EducationContentItemRepository items; private final ContentReviewRecordRepository reviews;
  private final AwarenessCampaignRepository campaigns; private final AuditService audit;
  public MedicalContentController(EducationContentItemRepository items, ContentReviewRecordRepository reviews,
                                  AwarenessCampaignRepository campaigns, AuditService audit) {
    this.items=items; this.reviews=reviews; this.campaigns=campaigns; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private EducationContentItem item(UUID id) {
    return items.findById(id).orElseThrow(() -> BizException.notFound("content_not_found","المحتوى غير موجود"));
  }

  public record ContentRequest(String title, String topic, String language, Boolean accessibleFormat, String sourceRef, String referencesText) {}
  @PostMapping("/")
  Map<String,Object> create(@RequestBody ContentRequest r) {
    var p = principal();
    var c = items.save(new EducationContentItem(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.topic(), r.language(),
      Boolean.TRUE.equals(r.accessibleFormat()), r.sourceRef(), r.referencesText(), p.email()));
    audit.record("content.drafted","education_content_item",c.getId().toString(),"{}");
    return Map.of("ok",true,"content",c);
  }
  public record ReviewRequest(String decision, String notes, LocalDate reviewedAt) {}
  @PostMapping("/{id}/review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody ReviewRequest r) {
    var p = principal(); var c = item(id);
    if (p.email() != null && p.email().equals(c.getAuthor()))
      throw BizException.forbidden("المؤلف لا يراجع محتواه بنفسه — المراجعة السريرية مستقلة");
    var rec = reviews.save(new ContentReviewRecord(UUID.fromString(p.facilityId()), p.governorate(), id, p.email(), r.decision(), r.notes()));
    if ("approved".equals(r.decision())) c.approve(p.email(), r.reviewedAt() == null ? LocalDate.now() : r.reviewedAt());
    else if ("rejected".equals(r.decision())) c.setStatus("draft");
    else c.setStatus("in_review");
    items.save(c);
    audit.record("content.reviewed","content_review_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"content",c,"review_due_at",String.valueOf(c.getReviewDueAt()));
  }
  @PostMapping("/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal(); var c = item(id);
    if (!ContentRules.publishable(c.getStatus(), c.getAuthor(), c.getClinicalReviewer(), c.getSourceRef(), c.getReferencesText(), c.getReviewedAt()))
      throw BizException.conflict("not_publishable","النشر يتطلب اعتمادًا سريريًا مستقلًا ومصدرًا ومراجع موثقة وتاريخ مراجعة");
    if (ContentRules.stale(c.getReviewDueAt(), LocalDate.now()))
      throw BizException.conflict("review_expired","انتهت صلاحية المراجعة — المحتوى المنتهي يُراجع أو يُسحب ولا يبقى منشورًا بصمت");
    c.setStatus("published"); items.save(c);
    audit.record("content.published","education_content_item",id.toString(),"{}");
    return Map.of("ok",true,"content",c);
  }
  @PostMapping("/{id}/withdraw")
  Map<String,Object> withdraw(@PathVariable UUID id) {
    principal(); var c = item(id);
    c.setStatus("withdrawn"); items.save(c);
    audit.record("content.withdrawn","education_content_item",id.toString(),"{}");
    return Map.of("ok",true,"content",c,"effect","withdrawn content is no longer served to patients");
  }
  @GetMapping("/{id}")
  Map<String,Object> get(@PathVariable UUID id) {
    principal(); var c = item(id);
    return Map.of("ok",true,"content",c,"review_stale",ContentRules.stale(c.getReviewDueAt(), LocalDate.now()));
  }
  public record CampaignRequest(String name, UUID contentId, String audience, LocalDate startsOn, LocalDate endsOn) {}
  @PostMapping("/campaigns")
  Map<String,Object> campaign(@RequestBody CampaignRequest r) {
    var p = principal(); var content = item(r.contentId());
    if (!"published".equals(content.getStatus()))
      throw BizException.conflict("content_not_published","الحملة تستخدم محتوى منشورًا معتمدًا فقط");
    var cp = campaigns.save(new AwarenessCampaign(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.contentId(), r.audience(), r.startsOn(), r.endsOn()));
    audit.record("content.campaign.created","awareness_campaign",cp.getId().toString(),"{}");
    return Map.of("ok",true,"campaign",cp);
  }
  public record CompleteRequest(BigDecimal comprehensionScore) {}
  @PostMapping("/campaigns/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id, @RequestBody CompleteRequest r) {
    principal();
    var cp = campaigns.findById(id).orElseThrow(() -> BizException.notFound("campaign_not_found","الحملة غير موجودة"));
    cp.complete(r.comprehensionScore()); campaigns.save(cp);
    boolean adequate = ContentRules.comprehensionAdequate(r.comprehensionScore() == null ? null : r.comprehensionScore().doubleValue());
    audit.record("content.campaign.completed","awareness_campaign",id.toString(),"{}");
    return Map.of("ok",true,"campaign",cp,"comprehension_adequate",adequate,
      "note",adequate ? "audience understood the material" : "comprehension under 70 — revise the material, reach alone is not success");
  }
}
