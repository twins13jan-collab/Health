package eg.gov.health.platform.notify;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "notification_outbox")
public class NotificationOutbox {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="recipient_ref") private String recipientRef;
  @Column(nullable=false) private String channel;
  private String subject; private String body;
  @Column(nullable=false) private String status = "queued";
  @Column(nullable=false) private int attempts = 0;
  @Column(name="last_error") private String lastError;
  @Column(name="sent_at") private OffsetDateTime sentAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NotificationOutbox() {}
  public NotificationOutbox(UUID facilityId, String governorate, String recipientRef, String channel, String subject, String body) {
    this.facilityId=facilityId; this.governorate=governorate; this.recipientRef=recipientRef; this.channel=channel; this.subject=subject; this.body=body;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;} public String getSubject(){return subject;} public String getBody(){return body;} public String getRecipientRef(){return recipientRef;} public String getChannel(){return channel;}
  public String getStatus(){return status;} public int getAttempts(){return attempts;} public String getLastError(){return lastError;}
  public OffsetDateTime getSentAt(){return sentAt;}
  public void markSent(){ this.status="sent"; this.sentAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
  public void markFailed(String error){ this.attempts++; this.lastError=error; this.status=NotificationRules.statusAfterFailure(this.attempts); this.updatedAt=OffsetDateTime.now(); }
}
