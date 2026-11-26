package eg.gov.health.platform.adt;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "inpatient_admissions")
public class InpatientAdmission {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="ward_id", nullable=false) private UUID wardId;
  @Column(name="bed_label", nullable=false) private String bedLabel;
  @Column(name="attending_name") private String attendingName;
  @Column(name="admission_reason") private String admissionReason;
  @Column(nullable=false) private String source = "clinic";
  @Column(nullable=false) private String status = "admitted";
  @Column(name="admitted_at") private OffsetDateTime admittedAt = OffsetDateTime.now();
  @Column(name="discharged_at") private OffsetDateTime dischargedAt;
  @Column(name="discharge_disposition") private String dischargeDisposition;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InpatientAdmission() {}
  public InpatientAdmission(UUID facilityId, String governorate, UUID patientId, UUID encounterId, UUID wardId,
                            String bedLabel, String attendingName, String admissionReason, String source) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.wardId=wardId; this.bedLabel=bedLabel; this.attendingName=attendingName; this.admissionReason=admissionReason;
    this.source=source==null?"clinic":source;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public UUID getPatientId(){return patientId;} public UUID getEncounterId(){return encounterId;}
  public UUID getWardId(){return wardId;} public String getBedLabel(){return bedLabel;}
  public String getAttendingName(){return attendingName;} public String getAdmissionReason(){return admissionReason;}
  public String getSource(){return source;} public String getStatus(){return status;}
  public OffsetDateTime getAdmittedAt(){return admittedAt;} public OffsetDateTime getDischargedAt(){return dischargedAt;}
  public String getDischargeDisposition(){return dischargeDisposition;}
  public void moveTo(UUID wardId, String bedLabel) { this.wardId=wardId; this.bedLabel=bedLabel; this.updatedAt=OffsetDateTime.now(); }
  public void discharge(String disposition) {
    this.status="discharged"; this.dischargeDisposition=disposition;
    this.dischargedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now();
  }
}
