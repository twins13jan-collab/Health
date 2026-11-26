package eg.gov.health.platform.chronicdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "home_measurements")
public class HomeMeasurement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="enrollment_id", nullable=false) private UUID enrollmentId;
  private String metric; private Double value;
  @Column(name="source_verified", nullable=false) private boolean sourceVerified = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HomeMeasurement() {}
  public HomeMeasurement(UUID facilityId, String governorate, UUID enrollmentId, String metric, Double value, boolean sourceVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.enrollmentId=enrollmentId; this.metric=metric; this.value=value;
    this.sourceVerified=sourceVerified;
  }
  public UUID getId(){return id;} public Double getValue(){return value;} public boolean isSourceVerified(){return sourceVerified;}
}
