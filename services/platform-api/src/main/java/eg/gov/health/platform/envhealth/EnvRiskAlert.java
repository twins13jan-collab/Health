package eg.gov.health.platform.envhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "env_risk_alerts")
public class EnvRiskAlert {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="hazard_type") private String hazardType;
  private String level;
  @Column(name="affected_population") private String affectedPopulation;
  @Column(name="feed_id") private UUID feedId;
  @Column(name="response_plan_activated", nullable=false) private boolean responsePlanActivated = false;
  @Column(nullable=false) private boolean published = false;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EnvRiskAlert() {}
  public EnvRiskAlert(UUID facilityId, String governorate, String hazardType, String level, String affectedPopulation, UUID feedId) {
    this.facilityId=facilityId; this.governorate=governorate; this.hazardType=hazardType; this.level=level; this.affectedPopulation=affectedPopulation; this.feedId=feedId;
  }
  public UUID getId(){return id;} public String getLevel(){return level;} public UUID getFeedId(){return feedId;}
  public boolean isResponsePlanActivated(){return responsePlanActivated;} public String getStatus(){return status;}
  public void activatePlan(){ this.responsePlanActivated=true; this.updatedAt=OffsetDateTime.now(); }
  public void publish(){ this.published=true; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
