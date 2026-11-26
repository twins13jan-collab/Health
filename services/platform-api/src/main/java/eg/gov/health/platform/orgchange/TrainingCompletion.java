package eg.gov.health.platform.orgchange;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "training_completions")
public class TrainingCompletion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="initiative_id", nullable=false) private UUID initiativeId;
  @Column(name="user_ref") private String userRef;
  @Column(name="user_role") private String userRole;
  @Column(nullable=false) private boolean completed = false;
  @Column(name="completed_at") private OffsetDateTime completedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TrainingCompletion() {}
  public TrainingCompletion(UUID facilityId, String governorate, UUID initiativeId, String userRef, String userRole, boolean completed) {
    this.facilityId=facilityId; this.governorate=governorate; this.initiativeId=initiativeId; this.userRef=userRef; this.userRole=userRole;
    this.completed=completed; if (completed) this.completedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
