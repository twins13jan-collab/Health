package eg.gov.health.platform.forensics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "forensic_reports")
public class ForensicReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="report_type") private String reportType;
  @Column(name="signed_by") private String signedBy;
  @Column(name="countersigned_by") private String countersignedBy;
  @Column(name="released_to") private String releasedTo;
  @Column(name="released_at") private OffsetDateTime releasedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ForensicReport() {}
  public ForensicReport(UUID facilityId, String governorate, UUID caseId, String reportType, String signedBy, String countersignedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.reportType=reportType; this.signedBy=signedBy; this.countersignedBy=countersignedBy;
  }
  public UUID getId(){return id;} public String getSignedBy(){return signedBy;} public String getCountersignedBy(){return countersignedBy;}
  public void release(String to){ this.releasedTo=to; this.releasedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
