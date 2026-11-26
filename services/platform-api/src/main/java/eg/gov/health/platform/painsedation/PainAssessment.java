package eg.gov.health.platform.painsedation;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pain_assessments")
public class PainAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String scale; private Integer score;
  @Column(name="age_group") private String ageGroup;
  @Column(name="assessed_at") private OffsetDateTime assessedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PainAssessment() {}
  public PainAssessment(UUID facilityId, String governorate, String patientRef, String scale, Integer score, String ageGroup) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.scale=scale; this.score=score; this.ageGroup=ageGroup;
    this.assessedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
