package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "inspections")
public class Inspection {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="receipt_id", nullable=false) private UUID receiptId;
  @Column(nullable=false) private String inspector;
  @Column(nullable=false) private String result;
  private String notes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected Inspection() {}
  public Inspection(UUID facilityId, String governorate, UUID receiptId, String inspector, String result, String notes) {
    this.facilityId=facilityId; this.governorate=governorate; this.receiptId=receiptId; this.inspector=inspector; this.result=result; this.notes=notes;
  }
  public UUID getId(){return id;} public String getResult(){return result;}
}
