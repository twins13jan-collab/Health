package eg.gov.health.platform.digitalquality;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "ux_quality_checks")
public class UxQualityCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String surface; private String environment;
  @Column(name="task_completion_pct") private BigDecimal taskCompletionPct;
  @Column(name="error_count") private Integer errorCount;
  @Column(name="p95_load_ms") private Integer p95LoadMs;
  @Column(name="api_connectivity_verified", nullable=false) private boolean apiConnectivityVerified = false;
  @Column(nullable=false) private boolean passed = false;
  @Column(name="checked_at") private OffsetDateTime checkedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected UxQualityCheck() {}
  public UxQualityCheck(UUID facilityId, String governorate, String surface, String environment, BigDecimal taskCompletionPct,
                        Integer errorCount, Integer p95LoadMs, boolean apiConnectivityVerified, boolean passed) {
    this.facilityId=facilityId; this.governorate=governorate; this.surface=surface; this.environment=environment;
    this.taskCompletionPct=taskCompletionPct; this.errorCount=errorCount; this.p95LoadMs=p95LoadMs;
    this.apiConnectivityVerified=apiConnectivityVerified; this.passed=passed; this.checkedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public boolean isPassed(){return passed;}
}
