package eg.gov.health.platform.nfr;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nfr_measurements")
public class NfrMeasurement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="requirement_id", nullable=false) private UUID requirementId;
  @Column(name="measured_value") private Double measuredValue;
  @Column(name="measured_on") private LocalDate measuredOn;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NfrMeasurement() {}
  public NfrMeasurement(UUID facilityId, String governorate, UUID requirementId, Double measuredValue, LocalDate measuredOn) {
    this.facilityId=facilityId; this.governorate=governorate; this.requirementId=requirementId; this.measuredValue=measuredValue; this.measuredOn=measuredOn;
  }
  public UUID getId(){return id;} public Double getMeasuredValue(){return measuredValue;} public LocalDate getMeasuredOn(){return measuredOn;}
}
