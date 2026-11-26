package eg.gov.health.platform.radiologydeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "imaging_reports")
public class ImagingReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  @Column(name="report_type") private String reportType;
  @Column(name="signed_by") private String signedBy;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ImagingReport() {}
  public ImagingReport(UUID facilityId, String governorate, UUID orderId, String reportType, String signedBy, UUID supersedesId) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.reportType=reportType;
    this.signedBy=signedBy; this.supersedesId=supersedesId;
  }
  public UUID getId(){return id;} public String getReportType(){return reportType;}
}
