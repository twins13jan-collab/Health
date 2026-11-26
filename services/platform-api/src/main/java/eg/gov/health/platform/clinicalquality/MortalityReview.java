package eg.gov.health.platform.clinicalquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mortality_reviews")
public class MortalityReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_ref") private String caseRef;
  @Column(name="review_done", nullable=false) private boolean reviewDone = false;
  private Boolean preventable;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MortalityReview() {}
  public MortalityReview(UUID facilityId, String governorate, String caseRef, boolean reviewDone, Boolean preventable) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseRef=caseRef; this.reviewDone=reviewDone; this.preventable=preventable;
  }
  public UUID getId(){return id;} public boolean isReviewDone(){return reviewDone;}
}
