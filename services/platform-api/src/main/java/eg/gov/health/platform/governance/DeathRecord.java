package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "death_records")
public class DeathRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="died_at", nullable=false) private OffsetDateTime diedAt;
  @Column(name="cause_icd10", nullable=false) private String causeIcd10;
  @Column(name="cause_description") private String causeDescription;
  @Column(name="certified_by", nullable=false) private String certifiedBy;
  @Column(nullable=false) private String status = "certified"; // certified, under_review, corrected
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeathRecord() {}
  public DeathRecord(UUID facilityId, String governorate, UUID patientId, OffsetDateTime diedAt, String causeIcd10, String causeDescription, String certifiedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.diedAt=diedAt;
    this.causeIcd10=causeIcd10; this.causeDescription=causeDescription; this.certifiedBy=certifiedBy;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public String getStatus(){return status;}
  public void correct(String icd10, String desc){this.causeIcd10=icd10; this.causeDescription=desc; this.status="corrected"; this.updatedAt=OffsetDateTime.now();}
}
