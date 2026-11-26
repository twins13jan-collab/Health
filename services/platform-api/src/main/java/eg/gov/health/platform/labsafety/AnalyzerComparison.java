package eg.gov.health.platform.labsafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "analyzer_comparisons")
public class AnalyzerComparison {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="introduction_id", nullable=false) private UUID introductionId;
  @Column(name="analyzer_a") private String analyzerA;
  @Column(name="analyzer_b") private String analyzerB;
  @Column(name="bias_pct") private BigDecimal biasPct;
  private Boolean acceptable;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AnalyzerComparison() {}
  public AnalyzerComparison(UUID facilityId, String governorate, UUID introductionId, String analyzerA, String analyzerB, BigDecimal biasPct, boolean acceptable) {
    this.facilityId=facilityId; this.governorate=governorate; this.introductionId=introductionId; this.analyzerA=analyzerA;
    this.analyzerB=analyzerB; this.biasPct=biasPct; this.acceptable=acceptable;
  }
  public UUID getId(){return id;}
}
