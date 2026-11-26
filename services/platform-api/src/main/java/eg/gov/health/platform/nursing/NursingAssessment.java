package eg.gov.health.platform.nursing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nursing_assessments")
public class NursingAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="fall_risk") private String fallRisk;
  @Column(name="pressure_ulcer_risk") private String pressureUlcerRisk;
  @Column(name="pain_score") private Integer painScore;
  @Column(name="nutrition_risk") private String nutritionRisk;
  private String mobility;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="assessed_at") private OffsetDateTime assessedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NursingAssessment() {}
  public NursingAssessment(UUID facilityId, String governorate, String patientRef, String fallRisk, String pressureUlcerRisk,
                           Integer painScore, String nutritionRisk, String mobility, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.fallRisk=fallRisk;
    this.pressureUlcerRisk=pressureUlcerRisk; this.painScore=painScore; this.nutritionRisk=nutritionRisk; this.mobility=mobility;
    this.assessedBy=assessedBy; this.assessedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
