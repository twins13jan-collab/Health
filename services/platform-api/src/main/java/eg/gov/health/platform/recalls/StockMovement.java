package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "stock_movements")
public class StockMovement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="batch_id", nullable=false) private UUID batchId;
  @Column(nullable=false) private String location;
  @Column(name="movement_type", nullable=false) private String movementType;
  @Column(nullable=false) private int quantity;
  @Column(nullable=false) private String unit = "unit";
  private java.math.BigDecimal cost;
  @Column(name="document_ref") private String documentRef;
  @Column(name="fefo_exception", nullable=false) private boolean fefoException = false;
  @Column(name="exception_reason") private String exceptionReason;
  @Column(name="approved_by") private String approvedBy;
  @Column(name="created_by") private String createdBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected StockMovement() {}
  public StockMovement(UUID facilityId, String governorate, UUID batchId, String location, String movementType, int quantity,
                       String unit, java.math.BigDecimal cost, String documentRef, boolean fefoException, String exceptionReason,
                       String approvedBy, String createdBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.batchId=batchId; this.location=location;
    this.movementType=movementType; this.quantity=quantity; this.unit=unit==null?"unit":unit; this.cost=cost;
    this.documentRef=documentRef; this.fefoException=fefoException; this.exceptionReason=exceptionReason;
    this.approvedBy=approvedBy; this.createdBy=createdBy;
  }
  public UUID getId(){return id;} public String getMovementType(){return movementType;} public int getQuantity(){return quantity;}
}
