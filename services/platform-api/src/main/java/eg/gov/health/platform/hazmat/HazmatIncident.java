package eg.gov.health.platform.hazmat;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "hazmat_incidents")
public class HazmatIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="agent_type") private String agentType;
  @Column(name="agent_name") private String agentName;
  @Column(name="exposure_scope") private String exposureScope;
  @Column(name="zones_established", nullable=false) private boolean zonesEstablished = false;
  @Column(nullable=false) private String status = "declared";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HazmatIncident() {}
  public HazmatIncident(UUID facilityId, String governorate, String agentType, String agentName, String exposureScope) {
    this.facilityId=facilityId; this.governorate=governorate; this.agentType=agentType; this.agentName=agentName; this.exposureScope=exposureScope;
  }
  public UUID getId(){return id;} public String getAgentType(){return agentType;} public boolean isZonesEstablished(){return zonesEstablished;} public String getStatus(){return status;}
  public void establishZones(){ this.zonesEstablished=true; this.status="responding"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
