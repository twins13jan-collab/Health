package eg.gov.health.platform.reports;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "data_snapshots")
public class DataSnapshot {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="indicator_id", nullable=false) private UUID indicatorId;
  @Column(name="cutoff_at") private OffsetDateTime cutoffAt = OffsetDateTime.now();
  @Column(name="participating_facilities", nullable=false) private int participatingFacilities = 0;
  @Column(name="expected_facilities", nullable=false) private int expectedFacilities = 0;
  @Column(name="coverage_pct", nullable=false) private java.math.BigDecimal coveragePct = java.math.BigDecimal.ZERO;
  @Column(name="data_class", nullable=false) private String dataClass = "incomplete";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DataSnapshot() {}
  public DataSnapshot(UUID facilityId, String governorate, UUID indicatorId, int participating, int expected, boolean anyLate) {
    this.facilityId=facilityId; this.governorate=governorate; this.indicatorId=indicatorId;
    this.participatingFacilities=participating; this.expectedFacilities=expected;
    double pct = ReportRules.coveragePct(participating, expected);
    this.coveragePct = java.math.BigDecimal.valueOf(pct);
    this.dataClass = ReportRules.dataClass(pct, anyLate);
  }
  public UUID getId(){return id;} public UUID getIndicatorId(){return indicatorId;} public UUID getFacilityId(){return facilityId;}
  public java.math.BigDecimal getCoveragePct(){return coveragePct;} public String getDataClass(){return dataClass;}
}
