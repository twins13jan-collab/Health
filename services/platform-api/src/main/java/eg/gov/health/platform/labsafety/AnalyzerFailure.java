package eg.gov.health.platform.labsafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "analyzer_failures")
public class AnalyzerFailure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String analyzer;
  @Column(name="failure_description") private String failureDescription;
  @Column(name="failed_at") private OffsetDateTime failedAt;
  @Column(name="impact_assessed", nullable=false) private boolean impactAssessed = false;
  @Column(name="affected_results_count") private Integer affectedResultsCount;
  @Column(name="returned_to_service", nullable=false) private boolean returnedToService = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AnalyzerFailure() {}
  public AnalyzerFailure(UUID facilityId, String governorate, String analyzer, String failureDescription) {
    this.facilityId=facilityId; this.governorate=governorate; this.analyzer=analyzer; this.failureDescription=failureDescription; this.failedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public boolean isImpactAssessed(){return impactAssessed;}
  public void assessImpact(int affected){ this.impactAssessed=true; this.affectedResultsCount=affected; this.updatedAt=OffsetDateTime.now(); }
  public void returnToService(){ this.returnedToService=true; this.updatedAt=OffsetDateTime.now(); }
}
