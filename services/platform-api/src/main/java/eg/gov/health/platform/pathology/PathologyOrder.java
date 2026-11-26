package eg.gov.health.platform.pathology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pathology_orders")
public class PathologyOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="requesting_physician") private String requestingPhysician;
  private String reason; private String site;
  @Column(name="specimen_type", nullable=false) private String specimenType;
  private String urgency;
  @Column(nullable=false) private String status = "ordered";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PathologyOrder() {}
  public PathologyOrder(UUID facilityId, String governorate, UUID patientId, String requestingPhysician, String reason,
                        String site, String specimenType, String urgency) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.requestingPhysician=requestingPhysician;
    this.reason=reason; this.site=site; this.specimenType=specimenType; this.urgency=urgency;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
