package eg.gov.health.platform.iamdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "entitlement_reviews")
public class EntitlementReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="subject_ref") private String subjectRef;
  @Column(name="reviewer_ref") private String reviewerRef;
  private String outcome;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EntitlementReview() {}
  public EntitlementReview(UUID facilityId, String governorate, String subjectRef, String reviewerRef, String outcome) {
    this.facilityId=facilityId; this.governorate=governorate; this.subjectRef=subjectRef; this.reviewerRef=reviewerRef; this.outcome=outcome;
  }
  public UUID getId(){return id;}
}
