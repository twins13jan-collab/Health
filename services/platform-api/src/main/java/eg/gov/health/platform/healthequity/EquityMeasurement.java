package eg.gov.health.platform.healthequity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "equity_measurements")
public class EquityMeasurement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String region; private Integer population;
  @Column(name="service_units") private Integer serviceUnits;
  @Column(name="disease_burden_index") private Double diseaseBurdenIndex;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EquityMeasurement() {}
  public EquityMeasurement(UUID facilityId, String governorate, String region, Integer population, Integer serviceUnits, Double diseaseBurdenIndex) {
    this.facilityId=facilityId; this.governorate=governorate; this.region=region; this.population=population; this.serviceUnits=serviceUnits;
    this.diseaseBurdenIndex=diseaseBurdenIndex;
  }
  public UUID getId(){return id;} public Integer getPopulation(){return population;} public Integer getServiceUnits(){return serviceUnits;} public Double getDiseaseBurdenIndex(){return diseaseBurdenIndex;}
}
