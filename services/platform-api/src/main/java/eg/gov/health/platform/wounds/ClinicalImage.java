package eg.gov.health.platform.wounds;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinical_images")
public class ClinicalImage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="assessment_id") private UUID assessmentId;
  @Column(name="consent_basis", nullable=false) private String consentBasis;
  private String purpose;
  @Column(name="body_site") private String bodySite;
  private String photographer;
  @Column(name="access_policy", nullable=false) private String accessPolicy = "restricted";
  @Column(name="captured_at") private OffsetDateTime capturedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClinicalImage() {}
  public ClinicalImage(UUID facilityId, String governorate, UUID caseId, UUID assessmentId, String consentBasis,
                       String purpose, String bodySite, String photographer) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.assessmentId=assessmentId;
    this.consentBasis=consentBasis; this.purpose=purpose; this.bodySite=bodySite; this.photographer=photographer;
  }
  public UUID getId(){return id;}
}
