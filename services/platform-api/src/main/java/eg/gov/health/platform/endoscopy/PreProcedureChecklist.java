package eg.gov.health.platform.endoscopy;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pre_procedure_checklists")
public class PreProcedureChecklist {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id", nullable=false) private UUID requestId;
  @Column(name="fasting_confirmed", nullable=false) private boolean fastingConfirmed = false;
  @Column(name="medications_reviewed", nullable=false) private boolean medicationsReviewed = false;
  @Column(name="allergy_checked", nullable=false) private boolean allergyChecked = false;
  @Column(name="consent_documented", nullable=false) private boolean consentDocumented = false;
  @Column(name="sedation_assessment") private String sedationAssessment;
  @Column(nullable=false) private boolean completed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PreProcedureChecklist() {}
  public PreProcedureChecklist(UUID facilityId, String governorate, UUID requestId, boolean fasting, boolean medications,
                               boolean allergy, boolean consent, String sedationAssessment) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.fastingConfirmed=fasting;
    this.medicationsReviewed=medications; this.allergyChecked=allergy; this.consentDocumented=consent;
    this.sedationAssessment=sedationAssessment;
    this.completed=EndoscopyRules.checklistComplete(fasting, medications, allergy, consent);
  }
  public UUID getId(){return id;} public boolean isCompleted(){return completed;}
}
