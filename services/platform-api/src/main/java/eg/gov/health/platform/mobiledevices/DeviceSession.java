package eg.gov.health.platform.mobiledevices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "device_sessions")
public class DeviceSession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_id", nullable=false) private UUID deviceId;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DeviceSession() {}
  public DeviceSession(UUID facilityId, String governorate, UUID deviceId) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceId=deviceId;
  }
  public UUID getId(){return id;}
  public void end(){ this.active=false; }
}
