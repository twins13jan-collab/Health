package eg.gov.health.platform.medicalrecords;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
/** ICD-10 coding of one encounter (Phase 4.9): a primary code that
 *  must exist in the national reference set (ref_icd10_codes) plus
 *  optional secondary codes. One coding row per encounter; recoding
 *  revises the row and is audited at the controller. */
@Entity @Table(name = "encounter_codings")
public class EncounterCoding {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="encounter_id", nullable=false, unique=true) private UUID encounterId;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="primary_icd10", nullable=false) private String primaryIcd10;
  @Column(name="secondary_icd10") private String secondaryIcd10;
  @Column(name="coded_by") private String codedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EncounterCoding() {}
  public EncounterCoding(UUID facilityId, String governorate, UUID encounterId, UUID patientId, String primaryIcd10, String secondaryIcd10, String codedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.encounterId=encounterId;
    this.patientId=patientId; this.primaryIcd10=primaryIcd10; this.secondaryIcd10=secondaryIcd10; this.codedBy=codedBy;
  }
  public UUID getId(){return id;} public UUID getEncounterId(){return encounterId;}
  public String getPrimaryIcd10(){return primaryIcd10;} public String getSecondaryIcd10(){return secondaryIcd10;}
  public String getCodedBy(){return codedBy;}
  public void revise(String primary, String secondary, String codedBy){
    this.primaryIcd10=primary; this.secondaryIcd10=secondary; this.codedBy=codedBy; this.updatedAt=OffsetDateTime.now();
  }
}
