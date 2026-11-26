package eg.gov.health.platform.workforcehealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ppe_issues")
public class PpeIssue {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_ref") private String workerRef;
  private String item;
  @Column(name="training_done", nullable=false) private boolean trainingDone = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PpeIssue() {}
  public PpeIssue(UUID facilityId, String governorate, String workerRef, String item, boolean trainingDone) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerRef=workerRef; this.item=item; this.trainingDone=trainingDone;
  }
  public UUID getId(){return id;}
}
