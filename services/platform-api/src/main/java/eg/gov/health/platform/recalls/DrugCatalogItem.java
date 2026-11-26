package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "drug_catalog_items")
public class DrugCatalogItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="generic_name", nullable=false) private String genericName;
  @Column(name="trade_name") private String tradeName;
  private String form; private String strength; private String barcode;
  @Column(name="registration_ref") private String registrationRef;
  private String source;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DrugCatalogItem() {}
  public DrugCatalogItem(UUID facilityId, String governorate, String genericName, String tradeName, String form, String strength, String barcode, String registrationRef, String source) {
    this.facilityId=facilityId; this.governorate=governorate; this.genericName=genericName; this.tradeName=tradeName;
    this.form=form; this.strength=strength; this.barcode=barcode; this.registrationRef=registrationRef; this.source=source;
  }
  public UUID getId(){return id;}
}
