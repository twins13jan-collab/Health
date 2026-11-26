package eg.gov.health.platform.healthequity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "access_gap_analyses")
public class AccessGapAnalysis {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String region; private String dimension;
  @Column(name="gap_pct") private Double gapPct;
  @Column(name="sample_size") private Integer sampleSize;
  @Column(nullable=false) private boolean published = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AccessGapAnalysis() {}
  public AccessGapAnalysis(UUID facilityId, String governorate, String region, String dimension, Double gapPct, Integer sampleSize) {
    this.facilityId=facilityId; this.governorate=governorate; this.region=region; this.dimension=dimension; this.gapPct=gapPct; this.sampleSize=sampleSize;
  }
  public UUID getId(){return id;} public Integer getSampleSize(){return sampleSize;}
  public void publish(){ this.published=true; }
}
