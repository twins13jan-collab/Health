package eg.gov.health.platform.observability;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ops_incidents")
public class OpsIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="component_id", nullable=false) private UUID componentId;
  private String severity;
  @Column(nullable=false) private String status = "open";
  @Column(name="resolution_note") private String resolutionNote;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected OpsIncident() {}
  public OpsIncident(UUID facilityId, String governorate, UUID componentId, String severity) {
    this.facilityId=facilityId; this.governorate=governorate; this.componentId=componentId; this.severity=severity;
  }
  public UUID getId(){return id;} public String getSeverity(){return severity;} public String getStatus(){return status;}
  public void escalate(){ this.status="escalated"; this.updatedAt=OffsetDateTime.now(); }
  public void close(String note){ this.status="closed"; this.resolutionNote=note; this.updatedAt=OffsetDateTime.now(); }
}
