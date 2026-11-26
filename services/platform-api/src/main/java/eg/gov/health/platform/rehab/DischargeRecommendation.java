package eg.gov.health.platform.rehab;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "discharge_recommendations")
public class DischargeRecommendation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(nullable=false) private String decision;
  @Column(name="home_plan") private String homePlan;
  @Column(name="assistive_devices") private String assistiveDevices;
  @Column(name="follow_up") private String followUp;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DischargeRecommendation() {}
  public DischargeRecommendation(UUID facilityId, String governorate, UUID planId, String decision, String homePlan, String devices, String followUp) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.decision=decision;
    this.homePlan=homePlan; this.assistiveDevices=devices; this.followUp=followUp;
  }
  public UUID getId(){return id;} public String getDecision(){return decision;}
}
