package eg.gov.health.platform.complexcare;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "complex_care_plans")
public class ComplexCarePlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String coordinator;
  @Column(name="conditions_summary") private String conditionsSummary;
  @Column(name="quality_of_life_score") private BigDecimal qualityOfLifeScore;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ComplexCarePlan() {}
  public ComplexCarePlan(UUID facilityId, String governorate, String patientRef, String coordinator, String conditionsSummary) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.coordinator=coordinator; this.conditionsSummary=conditionsSummary;
  }
  public UUID getId(){return id;} public String getCoordinator(){return coordinator;} public String getStatus(){return status;}
  public BigDecimal getQualityOfLifeScore(){return qualityOfLifeScore;}
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
  public void close(BigDecimal qol){ this.qualityOfLifeScore=qol; this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
