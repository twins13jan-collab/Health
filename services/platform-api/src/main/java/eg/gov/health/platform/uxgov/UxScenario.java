package eg.gov.health.platform.uxgov;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "ux_scenarios")
public class UxScenario {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="user_role") private String userRole;
  @Column(name="scenario_name") private String scenarioName;
  @Column(name="critical_task", nullable=false) private boolean criticalTask = false;
  @Column(name="target_seconds") private Integer targetSeconds;
  @Column(name="measured_seconds") private Integer measuredSeconds;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected UxScenario() {}
  public UxScenario(UUID facilityId, String governorate, String userRole, String scenarioName, boolean criticalTask, Integer targetSeconds, Integer measuredSeconds) {
    this.facilityId=facilityId; this.governorate=governorate; this.userRole=userRole; this.scenarioName=scenarioName;
    this.criticalTask=criticalTask; this.targetSeconds=targetSeconds; this.measuredSeconds=measuredSeconds;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public boolean isCriticalTask(){return criticalTask;} public Integer getTargetSeconds(){return targetSeconds;} public Integer getMeasuredSeconds(){return measuredSeconds;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
