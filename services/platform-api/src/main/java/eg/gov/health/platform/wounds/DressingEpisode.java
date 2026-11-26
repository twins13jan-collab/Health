package eg.gov.health.platform.wounds;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dressing_episodes")
public class DressingEpisode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(nullable=false) private String material;
  @Column(name="material_lot", nullable=false) private String materialLot;
  @Column(name="lot_status", nullable=false) private String lotStatus = "ok";
  @Column(nullable=false) private int quantity = 1;
  @Column(name="applied_by") private String appliedBy;
  private String response;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DressingEpisode() {}
  public DressingEpisode(UUID facilityId, String governorate, UUID caseId, String material, String materialLot,
                         String lotStatus, int quantity, String appliedBy, String response) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.material=material;
    this.materialLot=materialLot; this.lotStatus=lotStatus; this.quantity=quantity; this.appliedBy=appliedBy; this.response=response;
  }
  public UUID getId(){return id;}
}
