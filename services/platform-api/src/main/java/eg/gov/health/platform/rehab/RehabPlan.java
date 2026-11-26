package eg.gov.health.platform.rehab;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "rehab_plans")
public class RehabPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="referral_id", nullable=false) private UUID referralId;
  @Column(nullable=false) private String goals;
  @Column(name="baseline_summary") private String baselineSummary;
  private String responsible;
  @Column(name="target_adl_score") private Integer targetAdlScore;
  @Column(nullable=false) private String status = "draft";
  @Column(name="approved_by") private String approvedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RehabPlan() {}
  public RehabPlan(UUID facilityId, String governorate, UUID referralId, String goals, String baselineSummary, String responsible, Integer targetAdlScore) {
    this.facilityId=facilityId; this.governorate=governorate; this.referralId=referralId; this.goals=goals;
    this.baselineSummary=baselineSummary; this.responsible=responsible; this.targetAdlScore=targetAdlScore;
  }
  public UUID getId(){return id;} public UUID getReferralId(){return referralId;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;}
  public Integer getTargetAdlScore(){return targetAdlScore;}
  public void approve(String by){ this.status="approved"; this.approvedBy=by; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
