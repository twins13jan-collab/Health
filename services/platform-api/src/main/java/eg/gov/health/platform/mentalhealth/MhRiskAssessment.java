package eg.gov.health.platform.mentalhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mh_risk_assessments")
public class MhRiskAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="episode_id", nullable=false) private UUID episodeId;
  @Column(name="risk_level", nullable=false) private String riskLevel;
  @Column(name="protective_factors") private String protectiveFactors;
  @Column(name="safety_plan") private String safetyPlan;
  @Column(name="safety_protocol_activated", nullable=false) private boolean safetyProtocolActivated = false;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="assessed_at") private OffsetDateTime assessedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MhRiskAssessment() {}
  public MhRiskAssessment(UUID facilityId, String governorate, UUID episodeId, String riskLevel, String protectiveFactors, String safetyPlan, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.episodeId=episodeId; this.riskLevel=riskLevel;
    this.protectiveFactors=protectiveFactors; this.safetyPlan=safetyPlan;
    this.safetyProtocolActivated = MentalHealthRules.activatesSafetyProtocol(riskLevel);
    this.assessedBy=assessedBy;
  }
  public UUID getId(){return id;} public boolean isSafetyProtocolActivated(){return safetyProtocolActivated;} public String getRiskLevel(){return riskLevel;}
  public boolean hasSafetyPlan(){ return safetyPlan!=null && !safetyPlan.isBlank(); }
}
