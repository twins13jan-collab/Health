package eg.gov.health.platform.palliative;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "symptom_plans")
public class SymptomPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String symptom;
  private String goal; private String intervention; private String responsible;
  @Column(name="pain_before") private Integer painBefore;
  @Column(name="pain_after") private Integer painAfter;
  @Column(name="side_effects") private String sideEffects;
  @Column(name="review_due_at") private OffsetDateTime reviewDueAt;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SymptomPlan() {}
  public SymptomPlan(UUID facilityId, String governorate, UUID patientId, String symptom, String goal, String intervention,
                     String responsible, Integer painBefore, OffsetDateTime reviewDueAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.symptom=symptom;
    this.goal=goal; this.intervention=intervention; this.responsible=responsible; this.painBefore=painBefore; this.reviewDueAt=reviewDueAt;
  }
  public UUID getId(){return id;} public Integer getPainBefore(){return painBefore;} public String getStatus(){return status;}
  public void respond(Integer painAfter, String sideEffects){ this.painAfter=painAfter; this.sideEffects=sideEffects; this.status="responded"; this.updatedAt=OffsetDateTime.now(); }
}
