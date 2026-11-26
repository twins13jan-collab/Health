package eg.gov.health.platform.uxgov;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "ux_findings")
public class UxFinding {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="scenario_id", nullable=false) private UUID scenarioId;
  private String severity; private String description; private String resolution;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected UxFinding() {}
  public UxFinding(UUID facilityId, String governorate, UUID scenarioId, String severity, String description) {
    this.facilityId=facilityId; this.governorate=governorate; this.scenarioId=scenarioId; this.severity=severity; this.description=description;
  }
  public UUID getId(){return id;} public String getSeverity(){return severity;} public String getStatus(){return status;}
  public void address(String resolution){ this.resolution=resolution; this.status="addressed"; this.updatedAt=OffsetDateTime.now(); }
}
