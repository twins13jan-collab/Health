package eg.gov.health.platform.devicevigilance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "field_safety_actions")
public class FieldSafetyAction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="report_id", nullable=false) private UUID reportId;
  @Column(name="action_type") private String actionType;
  private String scope;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FieldSafetyAction() {}
  public FieldSafetyAction(UUID facilityId, String governorate, UUID reportId, String actionType, String scope) {
    this.facilityId=facilityId; this.governorate=governorate; this.reportId=reportId; this.actionType=actionType; this.scope=scope;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void complete(){ this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
