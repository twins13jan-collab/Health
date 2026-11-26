package eg.gov.health.platform.nutrition;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nutrition_assessments")
public class NutritionAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="weight_kg", nullable=false) private double weightKg;
  @Column(name="height_cm", nullable=false) private double heightCm;
  @Column(nullable=false) private double bmi;
  @Column(name="weight_loss_percent") private Double weightLossPercent;
  @Column(name="risk_level", nullable=false) private String riskLevel;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NutritionAssessment() {}
  public NutritionAssessment(UUID facilityId, String governorate, UUID patientId, double weightKg, double heightCm,
                             Double weightLossPercent, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.weightKg=weightKg;
    this.heightCm=heightCm; this.weightLossPercent=weightLossPercent; this.assessedBy=assessedBy;
    this.bmi=NutritionRules.bmi(weightKg, heightCm);
    this.riskLevel=NutritionRules.riskLevel(this.bmi, weightLossPercent);
  }
  public UUID getId(){return id;} public double getBmi(){return bmi;} public String getRiskLevel(){return riskLevel;} public double getWeightKg(){return weightKg;}
}
