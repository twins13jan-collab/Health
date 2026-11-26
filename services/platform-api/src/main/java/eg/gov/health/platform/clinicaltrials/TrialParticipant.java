package eg.gov.health.platform.clinicaltrials;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "trial_participants")
public class TrialParticipant {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="trial_id", nullable=false) private UUID trialId;
  @Column(name="participant_code", nullable=false) private String participantCode;
  @Column(name="eligibility_confirmed", nullable=false) private boolean eligibilityConfirmed = false;
  @Column(name="consent_status", nullable=false) private String consentStatus = "not_consented";
  @Column(name="consent_at") private OffsetDateTime consentAt;
  @Column(name="withdrawn_at") private OffsetDateTime withdrawnAt;
  @Column(name="withdrawal_reason") private String withdrawalReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TrialParticipant() {}
  public TrialParticipant(UUID facilityId, String governorate, UUID trialId, String participantCode, boolean eligibilityConfirmed) {
    this.facilityId=facilityId; this.governorate=governorate; this.trialId=trialId; this.participantCode=participantCode;
    this.eligibilityConfirmed=eligibilityConfirmed;
  }
  public UUID getId(){return id;} public String getConsentStatus(){return consentStatus;} public boolean isEligibilityConfirmed(){return eligibilityConfirmed;}
  public void consent(){ this.consentStatus="consented"; this.consentAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
  public void withdraw(String reason){ this.consentStatus="withdrawn"; this.withdrawnAt=OffsetDateTime.now(); this.withdrawalReason=reason; this.updatedAt=OffsetDateTime.now(); }
}
