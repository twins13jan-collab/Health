package eg.gov.health.platform.schoolhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "student_health_records")
public class StudentHealthRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="student_code", nullable=false) private String studentCode;
  @Column(name="institution_ref") private String institutionRef;
  @Column(name="guardian_consent", nullable=false) private boolean guardianConsent = false;
  @Column(name="chronic_conditions") private String chronicConditions;
  @Column(name="allergy_notes") private String allergyNotes;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StudentHealthRecord() {}
  public StudentHealthRecord(UUID facilityId, String governorate, String studentCode, String institutionRef, boolean guardianConsent,
                             String chronicConditions, String allergyNotes) {
    this.facilityId=facilityId; this.governorate=governorate; this.studentCode=studentCode; this.institutionRef=institutionRef;
    this.guardianConsent=guardianConsent; this.chronicConditions=chronicConditions; this.allergyNotes=allergyNotes;
  }
  public UUID getId(){return id;} public boolean isGuardianConsent(){return guardianConsent;}
}
