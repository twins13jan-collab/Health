package eg.gov.health.platform.infectioncontrol;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "antimicrobial_reviews")
public class AntimicrobialReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String antibiotic; private String indication;
  @Column(name="culture_result") private String cultureResult;
  @Column(name="narrower_option", nullable=false) private boolean narrowerOption = false;
  private String decision; @Column(name="reviewed_by") private String reviewedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AntimicrobialReview() {}
  public AntimicrobialReview(UUID facilityId, String governorate, String patientRef, String antibiotic, String indication, String cultureResult, boolean narrowerOption, String decision, String reviewedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.antibiotic=antibiotic; this.indication=indication;
    this.cultureResult=cultureResult; this.narrowerOption=narrowerOption; this.decision=decision; this.reviewedBy=reviewedBy;
  }
  public UUID getId(){return id;}
}
