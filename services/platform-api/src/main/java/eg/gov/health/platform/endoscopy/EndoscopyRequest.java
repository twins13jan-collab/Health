package eg.gov.health.platform.endoscopy;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "endoscopy_requests")
public class EndoscopyRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="scope_type", nullable=false) private String scopeType;
  private String indication; private String urgency;
  @Column(name="preparation_instructions") private String preparationInstructions;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EndoscopyRequest() {}
  public EndoscopyRequest(UUID facilityId, String governorate, UUID patientId, String scopeType, String indication, String urgency, String preparationInstructions) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.scopeType=scopeType;
    this.indication=indication; this.urgency=urgency; this.preparationInstructions=preparationInstructions;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
