package eg.gov.health.platform.healthequity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "coverage_programs")
public class CoverageProgram {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String region;
  @Column(name="program_type") private String programType;
  @Column(name="target_gap_pct") private Double targetGapPct;
  @Column(name="latest_measured_gap_pct") private Double latestMeasuredGapPct;
  @Column(nullable=false) private String status = "running";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CoverageProgram() {}
  public CoverageProgram(UUID facilityId, String governorate, String region, String programType, Double targetGapPct) {
    this.facilityId=facilityId; this.governorate=governorate; this.region=region; this.programType=programType; this.targetGapPct=targetGapPct;
  }
  public UUID getId(){return id;} public Double getTargetGapPct(){return targetGapPct;} public Double getLatestMeasuredGapPct(){return latestMeasuredGapPct;} public String getStatus(){return status;}
  public void recordGap(Double gap){ this.latestMeasuredGapPct=gap; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
