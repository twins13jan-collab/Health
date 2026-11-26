package eg.gov.health.platform.reports;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "correction_notices")
public class CorrectionNotice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="dataset_id", nullable=false) private UUID datasetId;
  @Column(nullable=false) private String reason;
  private String impact; @Column(name="replacement_version") private String replacementVersion;
  @Column(name="issued_by") private String issuedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CorrectionNotice() {}
  public CorrectionNotice(UUID facilityId, String governorate, UUID datasetId, String reason, String impact, String replacementVersion, String issuedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.datasetId=datasetId; this.reason=reason;
    this.impact=impact; this.replacementVersion=replacementVersion; this.issuedBy=issuedBy;
  }
  public UUID getId(){return id;}
}
