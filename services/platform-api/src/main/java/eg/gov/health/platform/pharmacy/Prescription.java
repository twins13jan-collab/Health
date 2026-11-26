package eg.gov.health.platform.pharmacy;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "prescriptions")
public class Prescription {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="drug_code", nullable=false) private String drugCode;
  @Column(name="drug_name") private String drugName;
  private String dose; private String frequency; private String duration;
  @Column(nullable=false) private String status = "prescribed"; // prescribed, verified, dispensed, partially_dispensed, returned, cancelled
  @Column(name="prescribed_by") private String prescribedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Prescription() {}
  public Prescription(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String drugCode, String drugName, String dose, String frequency, String duration, String prescribedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.drugCode=drugCode; this.drugName=drugName; this.dose=dose; this.frequency=frequency; this.duration=duration; this.prescribedBy=prescribedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public UUID getPatientId(){return patientId;} public String getDrugCode(){return drugCode;} public String getStatus()
  {return status;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}

  public String getDrugName(){return drugName;} public String getDose(){return dose;} public String getFrequency(){return frequency;} public String getDuration(){return duration;} public String getPrescribedBy(){return prescribedBy;} public java.time.OffsetDateTime getCreatedAt(){return createdAt;}
}
