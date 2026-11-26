package eg.gov.health.platform.followup;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "continuity_referrals")
public class ContinuityReferral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  private String target; @Column(name="referral_ref") private String referralRef;
  @Column(nullable=false) private boolean accepted = false;
  @Column(name="accepted_by") private String acceptedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ContinuityReferral() {}
  public ContinuityReferral(UUID facilityId, String governorate, UUID planId, String target, String referralRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.target=target; this.referralRef=referralRef;
  }
  public UUID getId(){return id;}
  public void accept(String by){ this.accepted=true; this.acceptedBy=by; }
}
