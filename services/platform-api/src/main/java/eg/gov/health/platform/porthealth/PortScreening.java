package eg.gov.health.platform.porthealth;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "port_screenings")
public class PortScreening {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="report_id", nullable=false) private UUID reportId;
  @Column(name="temperature_c") private BigDecimal temperatureC;
  @Column(name="screening_result") private String screeningResult;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PortScreening() {}
  public PortScreening(UUID facilityId, String governorate, UUID reportId, BigDecimal temperatureC, String screeningResult) {
    this.facilityId=facilityId; this.governorate=governorate; this.reportId=reportId; this.temperatureC=temperatureC; this.screeningResult=screeningResult;
  }
  public UUID getId(){return id;}
}
