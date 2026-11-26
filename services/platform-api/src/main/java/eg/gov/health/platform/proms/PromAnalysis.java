package eg.gov.health.platform.proms;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "prom_analyses")
public class PromAnalysis {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="care_pathway") private String carePathway;
  private String period;
  @Column(name="sample_size") private Integer sampleSize;
  @Column(name="response_rate_pct") private BigDecimal responseRatePct;
  @Column(name="mean_score") private BigDecimal meanScore;
  @Column(name="bias_note") private String biasNote;
  @Column(nullable=false) private boolean published = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PromAnalysis() {}
  public PromAnalysis(UUID facilityId, String governorate, String carePathway, String period, int sampleSize, BigDecimal responseRatePct, BigDecimal meanScore, String biasNote, boolean published) {
    this.facilityId=facilityId; this.governorate=governorate; this.carePathway=carePathway; this.period=period; this.sampleSize=sampleSize;
    this.responseRatePct=responseRatePct; this.meanScore=meanScore; this.biasNote=biasNote; this.published=published;
  }
  public UUID getId(){return id;} public boolean isPublished(){return published;}
}
