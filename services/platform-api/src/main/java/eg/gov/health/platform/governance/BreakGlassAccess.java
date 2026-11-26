package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "break_glass_access")
public class BreakGlassAccess {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="actor_id", nullable=false) private String actorId;
  @Column(nullable=false) private String reason;
  @Column(name="duration_minutes", nullable=false) private int durationMinutes;
  @Column(nullable=false) private String status = "active"; // active, expired, reviewed, flagged
  @Column(name="reviewed_by") private String reviewedBy;
  @Column(name="review_outcome") private String reviewOutcome;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected BreakGlassAccess() {}
  public BreakGlassAccess(UUID facilityId, String governorate, UUID patientId, String actorId, String reason, int durationMinutes) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.actorId=actorId;
    this.reason=reason; this.durationMinutes=durationMinutes;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void review(String by, String outcome){this.reviewedBy=by; this.reviewOutcome=outcome; this.status="flagged".equals(outcome)?"flagged":"reviewed"; this.updatedAt=OffsetDateTime.now();}
}
