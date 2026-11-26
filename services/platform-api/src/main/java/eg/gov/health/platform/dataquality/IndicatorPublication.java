package eg.gov.health.platform.dataquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "indicator_publications")
public class IndicatorPublication {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="indicator_id", nullable=false) private UUID indicatorId;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  private Double value;
  @Column(name="confidence_score") private Double confidenceScore;
  private String caveat;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IndicatorPublication() {}
  public IndicatorPublication(UUID facilityId, String governorate, UUID indicatorId, UUID assetId, Double value, Double confidenceScore, String caveat) {
    this.facilityId=facilityId; this.governorate=governorate; this.indicatorId=indicatorId; this.assetId=assetId; this.value=value;
    this.confidenceScore=confidenceScore; this.caveat=caveat;
  }
  public UUID getId(){return id;}
}
