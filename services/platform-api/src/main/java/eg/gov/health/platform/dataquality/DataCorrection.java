package eg.gov.health.platform.dataquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "data_corrections")
public class DataCorrection {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  @Column(name="issue_type") private String issueType;
  private String description;
  @Column(name="resolution_basis") private String resolutionBasis;
  @Column(nullable=false) private boolean corrected = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DataCorrection() {}
  public DataCorrection(UUID facilityId, String governorate, UUID assetId, String issueType, String description, String resolutionBasis, boolean corrected) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetId=assetId; this.issueType=issueType; this.description=description;
    this.resolutionBasis=resolutionBasis; this.corrected=corrected;
  }
  public UUID getId(){return id;}
}
