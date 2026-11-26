package eg.gov.health.platform.disasterops;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "disaster_events")
public class DisasterEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private Integer classification;
  @Column(name="plan_activated", nullable=false) private boolean planActivated = false;
  @Column(nullable=false) private String status = "declared";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DisasterEvent() {}
  public DisasterEvent(UUID facilityId, String governorate, String name, Integer classification) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.classification=classification;
  }
  public UUID getId(){return id;} public Integer getClassification(){return classification;} public String getStatus(){return status;}
  public void activatePlan(){ this.planActivated=true; this.status="responding"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
