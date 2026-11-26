package eg.gov.health.platform.patienttransport;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patient_transport_requests")
public class PatientTransportRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String purpose;
  @Column(name="need_type") private String needType;
  @Column(name="oxygen_needed", nullable=false) private boolean oxygenNeeded = false;
  @Column(name="scheduled_at") private OffsetDateTime scheduledAt;
  @Column(name="provider_id") private UUID providerId;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PatientTransportRequest() {}
  public PatientTransportRequest(UUID facilityId, String governorate, String patientRef, String purpose, String needType,
                          boolean oxygenNeeded, OffsetDateTime scheduledAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.purpose=purpose; this.needType=needType;
    this.oxygenNeeded=oxygenNeeded; this.scheduledAt=scheduledAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public boolean isOxygenNeeded(){return oxygenNeeded;}
  public String getNeedType(){return needType;} public OffsetDateTime getScheduledAt(){return scheduledAt;}
  public void schedule(UUID providerId){ this.providerId=providerId; this.status="scheduled"; this.updatedAt=OffsetDateTime.now(); }
  public void complete(){ this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
