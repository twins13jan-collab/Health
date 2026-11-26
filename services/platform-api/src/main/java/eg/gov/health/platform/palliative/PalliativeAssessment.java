package eg.gov.health.platform.palliative;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "palliative_assessments")
public class PalliativeAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  private String symptoms;
  @Column(name="pain_score") private Integer painScore;
  @Column(name="functional_status") private String functionalStatus;
  private String needs;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="assessed_at") private OffsetDateTime assessedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PalliativeAssessment() {}
  public PalliativeAssessment(UUID facilityId, String governorate, UUID patientId, String symptoms, Integer painScore, String functionalStatus, String needs, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.symptoms=symptoms;
    this.painScore=painScore; this.functionalStatus=functionalStatus; this.needs=needs; this.assessedBy=assessedBy;
  }
  public UUID getId(){return id;} public Integer getPainScore(){return painScore;}
}
