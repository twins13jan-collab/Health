package eg.gov.health.platform.palliative;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "goals_of_care")
public class GoalsOfCare {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String preferences;
  @Column(name="representative_name") private String representativeName;
  @Column(name="representative_verified", nullable=false) private boolean representativeVerified = false;
  @Column(name="documentation_source", nullable=false) private String documentationSource;
  @Column(name="review_date") private LocalDate reviewDate;
  @Column(name="disagreement_parties") private String disagreementParties;
  @Column(name="disagreement_legal_basis") private String disagreementLegalBasis;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected GoalsOfCare() {}
  public GoalsOfCare(UUID facilityId, String governorate, UUID patientId, String preferences, String representativeName,
                     boolean representativeVerified, String documentationSource, LocalDate reviewDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.preferences=preferences;
    this.representativeName=representativeName; this.representativeVerified=representativeVerified;
    this.documentationSource=documentationSource; this.reviewDate=reviewDate;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void contest(String parties, String legalBasis){ this.disagreementParties=parties; this.disagreementLegalBasis=legalBasis; this.status="contested"; this.updatedAt=OffsetDateTime.now(); }
}
