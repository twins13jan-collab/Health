package eg.gov.health.platform.medcontent;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "education_content_items")
public class EducationContentItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title; private String topic; private String language;
  @Column(name="accessible_format", nullable=false) private boolean accessibleFormat = false;
  @Column(name="source_ref") private String sourceRef;
  @Column(name="references_text") private String referencesText;
  private String author;
  @Column(name="clinical_reviewer") private String clinicalReviewer;
  @Column(name="reviewed_at") private LocalDate reviewedAt;
  @Column(name="review_due_at") private LocalDate reviewDueAt;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EducationContentItem() {}
  public EducationContentItem(UUID facilityId, String governorate, String title, String topic, String language, boolean accessibleFormat,
                              String sourceRef, String referencesText, String author) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.topic=topic; this.language=language;
    this.accessibleFormat=accessibleFormat; this.sourceRef=sourceRef; this.referencesText=referencesText; this.author=author;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getAuthor(){return author;}
  public String getClinicalReviewer(){return clinicalReviewer;} public String getSourceRef(){return sourceRef;}
  public String getReferencesText(){return referencesText;} public LocalDate getReviewedAt(){return reviewedAt;} public LocalDate getReviewDueAt(){return reviewDueAt;}
  public void approve(String reviewer, LocalDate reviewedAt){
    this.clinicalReviewer=reviewer; this.reviewedAt=reviewedAt; this.reviewDueAt=ContentRules.reviewDue(reviewedAt);
    this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
