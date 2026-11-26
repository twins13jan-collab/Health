package eg.gov.health.platform.devicevigilance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "vigilance_assessments")
public class VigilanceAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="report_id", nullable=false) private UUID reportId;
  @Column(name="risk_level") private String riskLevel;
  @Column(name="regulatory_report_required", nullable=false) private boolean regulatoryReportRequired = false;
  @Column(name="regulatory_reported", nullable=false) private boolean regulatoryReported = false;
  @Column(name="regulatory_reported_at") private OffsetDateTime regulatoryReportedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected VigilanceAssessment() {}
  public VigilanceAssessment(UUID facilityId, String governorate, UUID reportId, String riskLevel, boolean regulatoryReportRequired) {
    this.facilityId=facilityId; this.governorate=governorate; this.reportId=reportId; this.riskLevel=riskLevel; this.regulatoryReportRequired=regulatoryReportRequired;
  }
  public UUID getId(){return id;} public boolean isRegulatoryReportRequired(){return regulatoryReportRequired;} public boolean isRegulatoryReported(){return regulatoryReported;}
  public void markReported(){ this.regulatoryReported=true; this.regulatoryReportedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
