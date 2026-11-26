package eg.gov.health.platform.vendormonitoring;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "vendor_performance")
public class VendorPerformance {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="vendor_ref") private String vendorRef;
  @Column(name="on_time_percent") private Double onTimePercent;
  @Column(name="quality_score") private Double qualityScore;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected VendorPerformance() {}
  public VendorPerformance(UUID facilityId, String governorate, String vendorRef, Double onTimePercent, Double qualityScore) {
    this.facilityId=facilityId; this.governorate=governorate; this.vendorRef=vendorRef; this.onTimePercent=onTimePercent; this.qualityScore=qualityScore;
  }
  public UUID getId(){return id;} public Double getOnTimePercent(){return onTimePercent;} public Double getQualityScore(){return qualityScore;}
}
