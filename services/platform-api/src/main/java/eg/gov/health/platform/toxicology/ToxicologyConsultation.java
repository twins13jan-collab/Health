package eg.gov.health.platform.toxicology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "toxicology_consultations")
public class ToxicologyConsultation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  private String consultant; private String assessment; private String recommendation;
  @Column(name="reference_id") private UUID referenceId;
  @Column(name="center_unavailable", nullable=false) private boolean centerUnavailable = false;
  @Column(name="alternative_path") private String alternativePath;
  @Column(name="contact_time") private OffsetDateTime contactTime = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ToxicologyConsultation() {}
  public ToxicologyConsultation(UUID facilityId, String governorate, UUID caseId, String consultant, String assessment,
                                String recommendation, UUID referenceId, boolean centerUnavailable, String alternativePath) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.consultant=consultant;
    this.assessment=assessment; this.recommendation=recommendation; this.referenceId=referenceId;
    this.centerUnavailable=centerUnavailable; this.alternativePath=alternativePath;
  }
  public UUID getId(){return id;}
}
