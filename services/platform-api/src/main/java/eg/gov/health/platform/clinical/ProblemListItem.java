package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "problem_list")
public class ProblemListItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="icd10_code", nullable=false) private String icd10Code;
  @Column(nullable=false) private String description;
  @Column(name="onset_date") private LocalDate onsetDate;
  @Column(nullable=false) private String status = "active"; // active, resolved, inactive
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ProblemListItem() {}
  public ProblemListItem(UUID facilityId, String governorate, UUID patientId, String icd10Code, String description, LocalDate onsetDate, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.icd10Code=icd10Code;
    this.description=description; this.onsetDate=onsetDate; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
