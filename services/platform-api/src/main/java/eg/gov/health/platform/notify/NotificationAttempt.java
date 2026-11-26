package eg.gov.health.platform.notify;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "notification_attempts")
public class NotificationAttempt {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="notification_id", nullable=false) private UUID notificationId;
  private String channel; private String outcome; private String detail;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NotificationAttempt() {}
  public NotificationAttempt(UUID facilityId, String governorate, UUID notificationId, String channel, String outcome, String detail) {
    this.facilityId=facilityId; this.governorate=governorate; this.notificationId=notificationId; this.channel=channel; this.outcome=outcome; this.detail=detail;
  }
  public UUID getId(){return id;} public String getOutcome(){return outcome;}
}
