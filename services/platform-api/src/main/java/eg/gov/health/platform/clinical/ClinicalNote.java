package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "clinical_notes")
public class ClinicalNote {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(nullable=false) private String noteType = "progress"; // admission, progress, procedure, discharge, nursing
  @Column(name="subjective") private String subjective;
  @Column(name="objective") private String objective;
  @Column(name="assessment") private String assessment;
  @Column(name="plan") private String plan;
  @Column(name="author_id") private String authorId;
  @Column(nullable=false) private String status = "draft"; // draft, signed, amended
  @Column(name="signed_at") private OffsetDateTime signedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ClinicalNote() {}
  public ClinicalNote(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String noteType, String s, String o, String a, String p, String authorId) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.noteType=noteType==null?"progress":noteType; this.subjective=s; this.objective=o; this.assessment=a; this.plan=p; this.authorId=authorId;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void sign(){this.status="signed"; this.signedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now();}
  public void amend(String s, String o, String a, String p){this.subjective=s; this.objective=o; this.assessment=a; this.plan=p; this.status="amended"; this.updatedAt=OffsetDateTime.now();}
}
