package eg.gov.health.platform.workforcehealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "worker_support_records")
public class WorkerSupportRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_ref") private String workerRef;
  @Column(name="support_type") private String supportType;
  private String notes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected WorkerSupportRecord() {}
  public WorkerSupportRecord(UUID facilityId, String governorate, String workerRef, String supportType, String notes) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerRef=workerRef; this.supportType=supportType; this.notes=notes;
  }
  public UUID getId(){return id;}
}
