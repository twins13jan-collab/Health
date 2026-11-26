package eg.gov.health.platform.reports;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "aggregation_jobs")
public class AggregationJob {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="snapshot_id", nullable=false) private UUID snapshotId;
  @Column(name="rules_version", nullable=false) private int rulesVersion = 1;
  private String scope;
  @Column(name="numerator_value") private java.math.BigDecimal numeratorValue;
  @Column(name="denominator_value") private java.math.BigDecimal denominatorValue;
  @Column(name="computed_value") private java.math.BigDecimal computedValue;
  @Column(name="small_count_suppressed", nullable=false) private boolean smallCountSuppressed = false;
  @Column(nullable=false) private String status = "computed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AggregationJob() {}
  public AggregationJob(UUID facilityId, String governorate, UUID snapshotId, int rulesVersion, String scope,
                        java.math.BigDecimal numerator, java.math.BigDecimal denominator) {
    this.facilityId=facilityId; this.governorate=governorate; this.snapshotId=snapshotId; this.rulesVersion=rulesVersion;
    this.scope=scope; this.numeratorValue=numerator; this.denominatorValue=denominator;
    this.computedValue = ReportRules.compute(numerator, denominator);
    this.smallCountSuppressed = numerator != null && ReportRules.smallCountSuppressed(numerator.longValue());
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getSnapshotId(){return snapshotId;}
  public String getStatus(){return status;} public boolean isSmallCountSuppressed(){return smallCountSuppressed;}
  public java.math.BigDecimal getComputedValue(){return computedValue;}
  public void validate(){ this.status="validated"; this.updatedAt=OffsetDateTime.now(); }
  public void publish(){ this.status="published"; this.updatedAt=OffsetDateTime.now(); }
}
