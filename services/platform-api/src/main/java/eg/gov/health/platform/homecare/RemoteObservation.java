package eg.gov.health.platform.homecare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "remote_observations")
public class RemoteObservation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="device_code") private String deviceCode;
  @Column(nullable=false) private String kind;
  @Column(nullable=false) private double value;
  private String unit;
  @Column(name="source_time", nullable=false) private OffsetDateTime sourceTime;
  @Column(name="received_time") private OffsetDateTime receivedTime = OffsetDateTime.now();
  @Column(nullable=false) private String quality = "good";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RemoteObservation() {}
  public RemoteObservation(UUID facilityId, String governorate, UUID patientId, String deviceCode, String kind, double value, String unit, OffsetDateTime sourceTime, String quality) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.deviceCode=deviceCode;
    this.kind=kind; this.value=value; this.unit=unit; this.sourceTime=sourceTime; this.quality=quality;
  }
  public UUID getId(){return id;} public OffsetDateTime getSourceTime(){return sourceTime;} public String getKind(){return kind;} public double getValue(){return value;}
}
