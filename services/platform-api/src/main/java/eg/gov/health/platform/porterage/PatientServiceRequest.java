package eg.gov.health.platform.porterage;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patient_service_requests")
public class PatientServiceRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="request_type") private String requestType;
  private String detail;
  @Column(name="fulfilled_at") private OffsetDateTime fulfilledAt;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PatientServiceRequest() {}
  public PatientServiceRequest(UUID facilityId, String governorate, String patientRef, String requestType, String detail) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.requestType=requestType; this.detail=detail;
  }
  public UUID getId(){return id;}
  public String getPatientRef(){return patientRef;} public String getRequestType(){return requestType;} public String getDetail(){return detail;}
  public String getStatus(){return status;} public OffsetDateTime getFulfilledAt(){return fulfilledAt;} public OffsetDateTime getCreatedAt(){return createdAt;}
  public void fulfill(){ this.status="fulfilled"; this.fulfilledAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
