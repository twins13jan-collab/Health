package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "notifications")
public class Notification {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="recipient", nullable=false) private String recipient;
  @Column(nullable=false) private String channel = "in_app"; // in_app, sms, email
  @Column(nullable=false) private String subject;
  @Column(nullable=false) private String body;
  @Column(nullable=false) private String status = "queued"; // queued, sent, failed
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Notification() {}
  public Notification(UUID facilityId, String governorate, String recipient, String channel, String subject, String body) {
    this.facilityId=facilityId; this.governorate=governorate; this.recipient=recipient;
    this.channel=channel==null?"in_app":channel; this.subject=subject; this.body=body;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
