package eg.gov.health.platform.rehab;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "functional_assessments")
public class FunctionalAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="referral_id", nullable=false) private UUID referralId;
  @Column(nullable=false) private String kind = "baseline";
  @Column(name="mobility_score") private Integer mobilityScore;
  @Column(name="balance_score") private Integer balanceScore;
  @Column(name="pain_score") private Integer painScore;
  @Column(name="adl_score") private Integer adlScore;
  @Column(name="cognition_score") private Integer cognitionScore;
  @Column(name="communication_score") private Integer communicationScore;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="assessed_at") private OffsetDateTime assessedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected FunctionalAssessment() {}
  public FunctionalAssessment(UUID facilityId, String governorate, UUID referralId, String kind, Integer mobility, Integer balance,
                              Integer pain, Integer adl, Integer cognition, Integer communication, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.referralId=referralId; if (kind!=null) this.kind=kind;
    this.mobilityScore=mobility; this.balanceScore=balance; this.painScore=pain; this.adlScore=adl;
    this.cognitionScore=cognition; this.communicationScore=communication; this.assessedBy=assessedBy;
  }
  public UUID getId(){return id;} public Integer getAdlScore(){return adlScore;} public String getKind(){return kind;}
}
