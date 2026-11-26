package eg.gov.health.platform.envhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "water_safety_checks")
public class WaterSafetyCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="source_ref") private String sourceRef;
  @Column(name="check_type") private String checkType;
  private String result;
  @Column(name="action_taken") private String actionTaken;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WaterSafetyCheck() {}
  public WaterSafetyCheck(UUID facilityId, String governorate, String sourceRef, String checkType, String result) {
    this.facilityId=facilityId; this.governorate=governorate; this.sourceRef=sourceRef; this.checkType=checkType; this.result=result;
  }
  public UUID getId(){return id;} public String getResult(){return result;} public String getActionTaken(){return actionTaken;}
  public void act(String action){ this.actionTaken=action; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
