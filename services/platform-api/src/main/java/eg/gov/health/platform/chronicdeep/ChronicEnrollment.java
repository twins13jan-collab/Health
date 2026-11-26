package eg.gov.health.platform.chronicdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "chronic_enrollments")
public class ChronicEnrollment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="condition_name") private String conditionName;
  @Column(name="criteria_ref") private String criteriaRef;
  @Column(name="meets_criteria", nullable=false) private boolean meetsCriteria = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ChronicEnrollment() {}
  public ChronicEnrollment(UUID facilityId, String governorate, String patientRef, String conditionName, String criteriaRef, boolean meetsCriteria) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.conditionName=conditionName;
    this.criteriaRef=criteriaRef; this.meetsCriteria=meetsCriteria;
  }
  public UUID getId(){return id;} public String getCriteriaRef(){return criteriaRef;} public boolean isMeetsCriteria(){return meetsCriteria;}
}
