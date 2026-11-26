package eg.gov.health.platform.anomalyregistry;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "trend_analyses")
public class TrendAnalysis {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String period; private String region;
  @Column(name="confirmed_cases") private Integer confirmedCases;
  @Column(name="suspected_cases") private Integer suspectedCases;
  private String note;
  @Column(name="causal_claim") private String causalClaim;
  @Column(name="supporting_evidence") private String supportingEvidence;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TrendAnalysis() {}
  public TrendAnalysis(UUID facilityId, String governorate, String period, String region, int confirmedCases, int suspectedCases, String note, String causalClaim, String supportingEvidence) {
    this.facilityId=facilityId; this.governorate=governorate; this.period=period; this.region=region; this.confirmedCases=confirmedCases;
    this.suspectedCases=suspectedCases; this.note=note; this.causalClaim=causalClaim; this.supportingEvidence=supportingEvidence;
  }
  public UUID getId(){return id;} public Integer getConfirmedCases(){return confirmedCases;} public Integer getSuspectedCases(){return suspectedCases;}
}
