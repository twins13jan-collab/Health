package eg.gov.health.platform.sensory;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sensory_assessments")
public class SensoryAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String organ;
  @Column(nullable=false) private String side;
  private String measurements; private String method;
  @Column(name="device_code") private String deviceCode;
  private String unit;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(nullable=false) private String status = "valid";
  @Column(name="measured_by") private String measuredBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SensoryAssessment() {}
  public SensoryAssessment(UUID facilityId, String governorate, UUID patientId, String organ, String side, String measurements,
                           String method, String deviceCode, String unit, UUID supersedesId, String measuredBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.organ=organ; this.side=side;
    this.measurements=measurements; this.method=method; this.deviceCode=deviceCode; this.unit=unit;
    this.supersedesId=supersedesId; this.measuredBy=measuredBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public UUID getPatientId(){return patientId;}
  public void markCorrected(){ this.status="corrected"; }
}
