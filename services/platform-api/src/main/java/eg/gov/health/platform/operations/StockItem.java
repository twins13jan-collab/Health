package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "stock_items")
public class StockItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="item_code", nullable=false) private String itemCode;
  @Column(nullable=false) private String name;
  @Column(nullable=false) private int quantity = 0;
  @Column(name="reorder_level", nullable=false) private int reorderLevel = 0;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StockItem() {}
  public StockItem(UUID facilityId, String governorate, String itemCode, String name, int reorderLevel) {
    this.facilityId=facilityId; this.governorate=governorate; this.itemCode=itemCode; this.name=name; this.reorderLevel=reorderLevel;
  }
  public UUID getId(){return id;} public int getQuantity(){return quantity;} public String getItemCode(){return itemCode;}
  public int getReorderLevel(){return reorderLevel;} public UUID getFacilityId(){return facilityId;}
  public boolean applyMovement(int delta) {
    if (quantity + delta < 0) return false; // stock can never go negative — the movement is rejected, not clamped
    quantity += delta; updatedAt = OffsetDateTime.now(); return true;
  }
}
