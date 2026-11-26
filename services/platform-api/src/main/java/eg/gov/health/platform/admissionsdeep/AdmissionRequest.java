package eg.gov.health.platform.admissionsdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "admission_requests")
public class AdmissionRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String justification; private String priority; private String specialty;
  @Column(nullable=false) private boolean transfer = false;
  @Column(name="receiving_acceptance", nullable=false) private boolean receivingAcceptance = false;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AdmissionRequest() {}
  public AdmissionRequest(UUID facilityId, String governorate, String patientRef, String justification, String priority,
                            String specialty, boolean transfer, boolean receivingAcceptance) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.justification=justification;
    this.priority=priority; this.specialty=specialty; this.transfer=transfer; this.receivingAcceptance=receivingAcceptance;
  }
  public UUID getId(){return id;} public String getJustification(){return justification;} public boolean isTransfer(){return transfer;}
  public boolean isReceivingAcceptance(){return receivingAcceptance;}
  public void admit(){ this.status="admitted"; this.updatedAt=OffsetDateTime.now(); }
  public void discharge(){ this.status="discharged"; this.updatedAt=OffsetDateTime.now(); }
}
