package eg.gov.health.platform.occhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "worker_health_profiles")
public class WorkerHealthProfile {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_code", nullable=false) private String workerCode;
  private String sector;
  @Column(name="risk_level") private String riskLevel;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WorkerHealthProfile() {}
  public WorkerHealthProfile(UUID facilityId, String governorate, String workerCode, String sector, String riskLevel) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerCode=workerCode; this.sector=sector; this.riskLevel=riskLevel;
  }
  public UUID getId(){return id;}
}
