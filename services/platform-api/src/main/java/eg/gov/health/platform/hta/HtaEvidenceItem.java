package eg.gov.health.platform.hta;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "hta_evidence_items")
public class HtaEvidenceItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  private String source;
  @Column(name="study_type") private String studyType;
  @Column(name="quality_rating") private String qualityRating;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HtaEvidenceItem() {}
  public HtaEvidenceItem(UUID facilityId, String governorate, UUID assessmentId, String source, String studyType, String qualityRating) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.source=source; this.studyType=studyType; this.qualityRating=qualityRating;
  }
  public UUID getId(){return id;}
}
