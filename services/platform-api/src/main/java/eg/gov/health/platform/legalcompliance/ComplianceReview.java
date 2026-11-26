package eg.gov.health.platform.legalcompliance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "compliance_reviews")
public class ComplianceReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="requirement_id", nullable=false) private UUID requirementId;
  @Column(nullable=false) private boolean compliant = false;
  @Column(name="corrective_actions") private String correctiveActions;
  @Column(name="corrective_done", nullable=false) private boolean correctiveDone = false;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ComplianceReview() {}
  public ComplianceReview(UUID facilityId, String governorate, UUID requirementId, boolean compliant, String correctiveActions) {
    this.facilityId=facilityId; this.governorate=governorate; this.requirementId=requirementId; this.compliant=compliant; this.correctiveActions=correctiveActions;
  }
  public UUID getId(){return id;} public boolean isCompliant(){return compliant;} public boolean isCorrectiveDone(){return correctiveDone;}
  public void completeCorrective(){ this.correctiveDone=true; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
