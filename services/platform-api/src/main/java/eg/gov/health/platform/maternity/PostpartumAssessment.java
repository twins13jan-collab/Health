package eg.gov.health.platform.maternity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "postpartum_assessments")
public class PostpartumAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="pregnancy_id", nullable=false) private UUID pregnancyId;
  @Column(name="assessed_at") private OffsetDateTime assessedAt = OffsetDateTime.now();
  @Column(name="bleeding_status") private String bleedingStatus;
  @Column(name="bp_systolic") private Integer bpSystolic;
  @Column(name="bp_diastolic") private Integer bpDiastolic;
  @Column(name="pain_score") private Integer painScore;
  private String breastfeeding;
  @Column(name="mental_state") private String mentalState;
  @Column(name="danger_signs") private String dangerSigns;
  @Column(name="discharge_ready", nullable=false) private boolean dischargeReady = false;
  @Column(name="discharge_instructions") private String dischargeInstructions;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PostpartumAssessment() {}
  public PostpartumAssessment(UUID facilityId, String governorate, UUID pregnancyId, String bleeding, Integer sys, Integer dia,
                              Integer pain, String breastfeeding, String mental, String danger, boolean dischargeReady, String instructions) {
    this.facilityId=facilityId; this.governorate=governorate; this.pregnancyId=pregnancyId; this.bleedingStatus=bleeding;
    this.bpSystolic=sys; this.bpDiastolic=dia; this.painScore=pain; this.breastfeeding=breastfeeding;
    this.mentalState=mental; this.dangerSigns=danger; this.dischargeReady=dischargeReady; this.dischargeInstructions=instructions;
  }
  public UUID getId(){return id;} public boolean isDischargeReady(){return dischargeReady;}
  public UUID getPregnancyId(){return pregnancyId;} public OffsetDateTime getAssessedAt(){return assessedAt;}
}
