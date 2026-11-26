package eg.gov.health.platform.medicalassets;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "maintenance_plans")
public class MaintenancePlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  @Column(name="plan_type", nullable=false) private String planType;
  @Column(name="interval_days") private Integer intervalDays;
  @Column(name="next_due") private LocalDate nextDue;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MaintenancePlan() {}
  public MaintenancePlan(UUID facilityId, String governorate, UUID assetId, String planType, Integer intervalDays, LocalDate nextDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetId=assetId; this.planType=planType; this.intervalDays=intervalDays; this.nextDue=nextDue;
  }
  public UUID getId(){return id;}
  public UUID getAssetId(){return assetId;} public String getPlanType(){return planType;}
  public Integer getIntervalDays(){return intervalDays;} public LocalDate getNextDue(){return nextDue;}
}
