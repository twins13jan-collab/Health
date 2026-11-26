package eg.gov.health.platform.criticalcare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "icu_stays")
public class IcuStay {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="admission_reason", nullable=false) private String admissionReason;
  @Column(name="care_level", nullable=false) private String careLevel = "level_3";
  private String team;
  @Column(name="primary_diagnosis") private String primaryDiagnosis;
  @Column(name="reference_weight_kg") private Double referenceWeightKg;
  @Column(name="weight_unit", nullable=false) private String weightUnit = "kg";
  @Column(name="weight_verified", nullable=false) private boolean weightVerified = false;
  @Column(nullable=false) private String status = "active";
  @Column(name="admitted_at") private OffsetDateTime admittedAt = OffsetDateTime.now();
  @Column(name="ended_at") private OffsetDateTime endedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IcuStay() {}
  public IcuStay(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String admissionReason,
                 String careLevel, String team, String primaryDiagnosis, Double referenceWeightKg, String weightUnit, boolean weightVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.admissionReason=admissionReason; if (careLevel!=null) this.careLevel=careLevel; this.team=team;
    this.primaryDiagnosis=primaryDiagnosis; this.referenceWeightKg=referenceWeightKg;
    if (weightUnit!=null) this.weightUnit=weightUnit; this.weightVerified=weightVerified;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public String getStatus(){return status;} public boolean isWeightVerified(){return weightVerified;}
  public Double getReferenceWeightKg(){return referenceWeightKg;} public String getWeightUnit(){return weightUnit;}
  public String getAdmissionReason(){return admissionReason;} public String getCareLevel(){return careLevel;}
  public String getTeam(){return team;} public String getPrimaryDiagnosis(){return primaryDiagnosis;}
  public OffsetDateTime getAdmittedAt(){return admittedAt;}
  public void endStay(String to){ this.status=to; this.endedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
