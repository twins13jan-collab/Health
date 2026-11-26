package eg.gov.health.platform.mobiledevices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "push_receipts")
public class PushReceipt {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_id", nullable=false) private UUID deviceId;
  @Column(name="notification_ref") private String notificationRef;
  @Column(nullable=false) private boolean delivered = false;
  @Column(nullable=false) private boolean acknowledged = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PushReceipt() {}
  public PushReceipt(UUID facilityId, String governorate, UUID deviceId, String notificationRef, boolean delivered, boolean acknowledged) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceId=deviceId; this.notificationRef=notificationRef;
    this.delivered=delivered; this.acknowledged=acknowledged;
  }
  public UUID getId(){return id;}
}
