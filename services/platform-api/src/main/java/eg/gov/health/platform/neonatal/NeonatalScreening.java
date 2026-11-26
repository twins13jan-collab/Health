package eg.gov.health.platform.neonatal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "neonatal_screenings")
public class NeonatalScreening {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="newborn_id", nullable=false) private UUID newbornId;
  @Column(name="screening_type", nullable=false) private String screeningType;
  @Column(name="sample_at") private OffsetDateTime sampleAt;
  @Column(nullable=false) private String result = "pending";
  @Column(name="result_at") private OffsetDateTime resultAt;
  @Column(name="recollect_required", nullable=false) private boolean recollectRequired = false;
  @Column(name="referral_status", nullable=false) private String referralStatus = "none";
  @Column(name="referral_outcome") private String referralOutcome;
  @Column(name="acknowledged_by") private String acknowledgedBy;
  @Column(name="acknowledged_at") private OffsetDateTime acknowledgedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NeonatalScreening() {}
  public NeonatalScreening(UUID facilityId, String governorate, UUID newbornId, String screeningType, OffsetDateTime sampleAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.newbornId=newbornId; this.screeningType=screeningType; this.sampleAt=sampleAt;
  }
  public UUID getId(){return id;} public String getResult(){return result;} public String getReferralStatus(){return referralStatus;}
  public boolean isRecollectRequired(){return recollectRequired;}
  public void applyResult(NeonatalRules.ScreeningOutcome o, String acknowledgedBy) {
    this.result=o.result(); this.resultAt=OffsetDateTime.now();
    this.recollectRequired=o.recollectRequired();
    if (o.referralOpens()) this.referralStatus="open";
    if (acknowledgedBy!=null) { this.acknowledgedBy=acknowledgedBy; this.acknowledgedAt=OffsetDateTime.now(); }
    this.updatedAt=OffsetDateTime.now();
  }
  public void closeReferral(String outcome){ this.referralStatus="closed"; this.referralOutcome=outcome; this.updatedAt=OffsetDateTime.now(); }
}
