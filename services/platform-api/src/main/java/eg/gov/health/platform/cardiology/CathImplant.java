package eg.gov.health.platform.cardiology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cath_implants")
public class CathImplant {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="procedure_id", nullable=false) private UUID procedureId;
  @Column(name="item_type", nullable=false) private String itemType;
  private String description; @Column(name="serial_lot", nullable=false) private String serialLot;
  @Column(name="cost_ref") private String costRef; @Column(name="claim_ref") private String claimRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CathImplant() {}
  public CathImplant(UUID facilityId, String governorate, UUID procedureId, String itemType, String description, String serialLot, String costRef, String claimRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.procedureId=procedureId; this.itemType=itemType;
    this.description=description; this.serialLot=serialLot; this.costRef=costRef; this.claimRef=claimRef;
  }
  public UUID getId(){return id;}
}
