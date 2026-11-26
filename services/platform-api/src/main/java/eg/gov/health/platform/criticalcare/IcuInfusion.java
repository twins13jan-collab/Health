package eg.gov.health.platform.criticalcare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "icu_infusions")
public class IcuInfusion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="stay_id", nullable=false) private UUID stayId;
  @Column(name="drug_code", nullable=false) private String drugCode;
  private String concentration;
  @Column(name="dose_rate", nullable=false) private double doseRate;
  @Column(name="rate_unit", nullable=false) private String rateUnit;
  @Column(name="reference_weight_kg") private Double referenceWeightKg;
  @Column(nullable=false) private String status = "running";
  @Column(name="started_at") private OffsetDateTime startedAt = OffsetDateTime.now();
  @Column(name="ended_at") private OffsetDateTime endedAt;
  @Column(name="started_by") private String startedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IcuInfusion() {}
  public IcuInfusion(UUID facilityId, String governorate, UUID stayId, String drugCode, String concentration,
                     double doseRate, String rateUnit, Double referenceWeightKg, String startedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.stayId=stayId; this.drugCode=drugCode;
    this.concentration=concentration; this.doseRate=doseRate; this.rateUnit=rateUnit;
    this.referenceWeightKg=referenceWeightKg; this.startedBy=startedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public String getDrugCode(){return drugCode;} public double getDoseRate(){return doseRate;}
  public String getRateUnit(){return rateUnit;} public OffsetDateTime getStartedAt(){return startedAt;}
  public void stop(String to){ this.status=to; this.endedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
