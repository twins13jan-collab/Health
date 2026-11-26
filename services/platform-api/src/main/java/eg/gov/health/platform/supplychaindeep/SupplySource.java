package eg.gov.health.platform.supplychaindeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "supply_sources")
public class SupplySource {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="item_ref") private String itemRef;
  @Column(name="supplier_ref") private String supplierRef;
  @Column(name="regulatory_approved", nullable=false) private boolean regulatoryApproved = false;
  @Column(name="share_percent") private Integer sharePercent;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SupplySource() {}
  public SupplySource(UUID facilityId, String governorate, String itemRef, String supplierRef, boolean regulatoryApproved, Integer sharePercent) {
    this.facilityId=facilityId; this.governorate=governorate; this.itemRef=itemRef; this.supplierRef=supplierRef;
    this.regulatoryApproved=regulatoryApproved; this.sharePercent=sharePercent;
  }
  public UUID getId(){return id;} public Integer getSharePercent(){return sharePercent;} public boolean isRegulatoryApproved(){return regulatoryApproved;}
}
