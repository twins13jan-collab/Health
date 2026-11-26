package eg.gov.health.platform.homecare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "home_care_plans")
public class HomeCarePlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="referral_id", nullable=false) private UUID referralId;
  private String goals; private String frequency; private String instructions;
  @Column(name="escalation_signs") private String escalationSigns;
  @Column(name="approved_by", nullable=false) private String approvedBy;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HomeCarePlan() {}
  public HomeCarePlan(UUID facilityId, String governorate, UUID referralId, String goals, String frequency, String instructions, String escalationSigns, String approvedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.referralId=referralId; this.goals=goals;
    this.frequency=frequency; this.instructions=instructions; this.escalationSigns=escalationSigns; this.approvedBy=approvedBy;
  }
  public UUID getId(){return id;} public UUID getReferralId(){return referralId;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
