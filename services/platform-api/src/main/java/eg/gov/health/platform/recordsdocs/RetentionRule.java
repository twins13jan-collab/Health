package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "retention_rules")
public class RetentionRule {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="doc_type", nullable=false) private String docType;
  @Column(name="retention_years", nullable=false) private int retentionYears;
  @Column(name="legal_basis") private String legalBasis;
  @Column(name="managed_by") private String managedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RetentionRule() {}
  public RetentionRule(UUID facilityId, String governorate, String docType, int retentionYears, String legalBasis, String managedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.docType=docType; this.retentionYears=retentionYears; this.legalBasis=legalBasis; this.managedBy=managedBy;
  }
  public UUID getId(){return id;} public int getRetentionYears(){return retentionYears;}
}
