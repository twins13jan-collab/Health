package eg.gov.health.platform.emergency;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "dispatch_requests")
public class DispatchRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(nullable=false) private Double lat; @Column(nullable=false) private Double lon;
  @Column(nullable=false) private String complaint;
  @Column(name="required_capability") private String requiredCapability;
  @Column(name="assigned_unit_id") private UUID assignedUnitId;
  @Column(nullable=false) private String status = "received"; // received, assigned, en_route, on_scene, transporting, completed, cancelled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DispatchRequest() {}
  public DispatchRequest(UUID facilityId, String governorate, UUID patientId, Double lat, Double lon, String complaint, String requiredCapability) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.lat=lat; this.lon=lon;
    this.complaint=complaint; this.requiredCapability=requiredCapability;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void assign(UUID unitId){this.assignedUnitId=unitId; this.status="assigned"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
