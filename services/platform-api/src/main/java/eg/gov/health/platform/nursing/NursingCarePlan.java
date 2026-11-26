package eg.gov.health.platform.nursing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nursing_care_plans")
public class NursingCarePlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String goal; private String interventions;
  @Column(name="reassessment_due") private LocalDate reassessmentDue;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NursingCarePlan() {}
  public NursingCarePlan(UUID facilityId, String governorate, String patientRef, String goal, String interventions, LocalDate reassessmentDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.goal=goal; this.interventions=interventions; this.reassessmentDue=reassessmentDue;
  }
  public UUID getId(){return id;}
}
