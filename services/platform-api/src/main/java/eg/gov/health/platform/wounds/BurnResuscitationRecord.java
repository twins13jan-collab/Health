package eg.gov.health.platform.wounds;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "burn_resuscitation_records")
public class BurnResuscitationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="weight_kg", nullable=false) private double weightKg;
  @Column(name="tbsa_percent", nullable=false) private double tbsaPercent;
  @Column(name="planned_ml_24h", nullable=false) private double plannedMl24h;
  @Column(name="planned_ml_first_8h", nullable=false) private double plannedMlFirst8h;
  @Column(name="actual_ml") private Double actualMl;
  @Column(name="urine_output_ml_hr") private Double urineOutputMlHr;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected BurnResuscitationRecord() {}
  public BurnResuscitationRecord(UUID facilityId, String governorate, UUID caseId, double weightKg, double tbsaPercent) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.weightKg=weightKg; this.tbsaPercent=tbsaPercent;
    this.plannedMl24h=WoundRules.parklandMl24h(weightKg, tbsaPercent);
    this.plannedMlFirst8h=WoundRules.parklandFirst8h(weightKg, tbsaPercent);
  }
  public UUID getId(){return id;} public double getPlannedMl24h(){return plannedMl24h;} public double getPlannedMlFirst8h(){return plannedMlFirst8h;}
  public void recordActuals(Double actualMl, Double urineOutputMlHr){ this.actualMl=actualMl; this.urineOutputMlHr=urineOutputMlHr; this.updatedAt=OffsetDateTime.now(); }
}
