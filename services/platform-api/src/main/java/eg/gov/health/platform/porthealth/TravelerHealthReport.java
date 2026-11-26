package eg.gov.health.platform.porthealth;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "traveler_health_reports")
public class TravelerHealthReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="traveler_code", nullable=false) private String travelerCode;
  @Column(name="travel_ref") private String travelRef;
  @Column(name="origin_country") private String originCountry;
  private String symptoms;
  @Column(name="risk_level") private String riskLevel;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TravelerHealthReport() {}
  public TravelerHealthReport(UUID facilityId, String governorate, String travelerCode, String travelRef, String originCountry, String symptoms, String riskLevel) {
    this.facilityId=facilityId; this.governorate=governorate; this.travelerCode=travelerCode; this.travelRef=travelRef;
    this.originCountry=originCountry; this.symptoms=symptoms; this.riskLevel=riskLevel;
  }
  public UUID getId(){return id;} public String getRiskLevel(){return riskLevel;}
}
