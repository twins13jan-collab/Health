package eg.gov.health.platform.donationcampaigns;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "donor_registrations")
public class DonorRegistration {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="campaign_id", nullable=false) private UUID campaignId;
  @Column(name="donor_ref") private String donorRef;
  @Column(name="last_donation_date") private LocalDate lastDonationDate;
  @Column(name="age_ok") private Boolean ageOk;
  @Column(name="weight_ok") private Boolean weightOk;
  @Column(name="hemoglobin_ok") private Boolean hemoglobinOk;
  @Column(name="health_ok") private Boolean healthOk;
  @Column(nullable=false) private boolean eligible = false;
  @Column(name="deferred_reason") private String deferredReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DonorRegistration() {}
  public DonorRegistration(UUID facilityId, String governorate, UUID campaignId, String donorRef, LocalDate lastDonationDate,
                           Boolean ageOk, Boolean weightOk, Boolean hemoglobinOk, Boolean healthOk, boolean eligible, String deferredReason) {
    this.facilityId=facilityId; this.governorate=governorate; this.campaignId=campaignId; this.donorRef=donorRef; this.lastDonationDate=lastDonationDate;
    this.ageOk=ageOk; this.weightOk=weightOk; this.hemoglobinOk=hemoglobinOk; this.healthOk=healthOk; this.eligible=eligible; this.deferredReason=deferredReason;
  }
  public UUID getId(){return id;} public boolean isEligible(){return eligible;}
}
