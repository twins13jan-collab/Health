package eg.gov.health.platform.connecteddevices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "device_readings")
public class DeviceReading {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_id", nullable=false) private UUID deviceId;
  private String metric;
  @Column(name="raw_value") private Double rawValue;
  private String unit;
  @Column(name="signal_quality") private String signalQuality;
  @Column(nullable=false) private boolean reliable = true;
  @Column(nullable=false) private boolean anomaly = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DeviceReading() {}
  public DeviceReading(UUID facilityId, String governorate, UUID deviceId, String metric, Double rawValue, String unit,
                         String signalQuality, boolean reliable, boolean anomaly) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceId=deviceId; this.metric=metric; this.rawValue=rawValue;
    this.unit=unit; this.signalQuality=signalQuality; this.reliable=reliable; this.anomaly=anomaly;
  }
  public UUID getId(){return id;} public boolean isReliable(){return reliable;} public boolean isAnomaly(){return anomaly;}
}
