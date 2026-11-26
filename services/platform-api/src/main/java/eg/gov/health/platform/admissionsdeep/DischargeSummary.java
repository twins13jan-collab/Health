package eg.gov.health.platform.admissionsdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "discharge_summaries")
public class DischargeSummary {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="admission_id", nullable=false) private UUID admissionId;
  private String diagnoses; private String procedures; private String instructions; private String warnings;
  @Column(name="medications_plan") private String medicationsPlan;
  @Column(name="followup_plan") private String followupPlan;
  @Column(nullable=false) private boolean finalized = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DischargeSummary() {}
  public DischargeSummary(UUID facilityId, String governorate, UUID admissionId, String diagnoses, String procedures,
                            String instructions, String warnings, String medicationsPlan, String followupPlan) {
    this.facilityId=facilityId; this.governorate=governorate; this.admissionId=admissionId; this.diagnoses=diagnoses;
    this.procedures=procedures; this.instructions=instructions; this.warnings=warnings; this.medicationsPlan=medicationsPlan;
    this.followupPlan=followupPlan;
  }
  public UUID getId(){return id;} public String getDiagnoses(){return diagnoses;} public String getProcedures(){return procedures;}
  public String getInstructions(){return instructions;} public String getWarnings(){return warnings;}
  public String getMedicationsPlan(){return medicationsPlan;} public String getFollowupPlan(){return followupPlan;}
  public void finalizeSummary(){ this.finalized=true; }
}
