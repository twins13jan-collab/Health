package eg.gov.health.platform.followup;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pending_results_reviews")
public class PendingResultReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="test_name") private String testName;
  @Column(name="resulted_at") private OffsetDateTime resultedAt;
  @Column(nullable=false) private boolean abnormal = false;
  @Column(name="reviewed_by") private String reviewedBy;
  @Column(name="action_taken") private String actionTaken;
  @Column(name="patient_notified", nullable=false) private boolean patientNotified = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PendingResultReview() {}
  public PendingResultReview(UUID facilityId, String governorate, UUID planId, String testName, boolean abnormal) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.testName=testName; this.abnormal=abnormal; this.resultedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public boolean isAbnormal(){return abnormal;} public String getReviewedBy(){return reviewedBy;} public boolean isPatientNotified(){return patientNotified;}
  public void review(String by, String action, boolean notified){ this.reviewedBy=by; this.actionTaken=action; this.patientNotified=notified; }
}
