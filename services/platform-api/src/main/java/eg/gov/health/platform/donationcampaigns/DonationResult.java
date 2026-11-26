package eg.gov.health.platform.donationcampaigns;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "donation_results")
public class DonationResult {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="registration_id", nullable=false) private UUID registrationId;
  @Column(name="test_result") private String testResult;
  @Column(name="donor_notified_confidentially", nullable=false) private boolean donorNotifiedConfidentially = false;
  @Column(name="referral_created", nullable=false) private boolean referralCreated = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DonationResult() {}
  public DonationResult(UUID facilityId, String governorate, UUID registrationId, String testResult, boolean notified, boolean referral) {
    this.facilityId=facilityId; this.governorate=governorate; this.registrationId=registrationId; this.testResult=testResult;
    this.donorNotifiedConfidentially=notified; this.referralCreated=referral;
  }
  public UUID getId(){return id;}
}
