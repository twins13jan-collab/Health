package eg.gov.health.platform.complexcare;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "home_equipment_orders")
public class HomeEquipmentOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  private String equipment; private String supplier;
  @Column(nullable=false) private boolean delivered = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HomeEquipmentOrder() {}
  public HomeEquipmentOrder(UUID facilityId, String governorate, UUID planId, String equipment, String supplier) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.equipment=equipment; this.supplier=supplier;
  }
  public UUID getId(){return id;}
  public void deliver(){ this.delivered=true; this.updatedAt=OffsetDateTime.now(); }
}
