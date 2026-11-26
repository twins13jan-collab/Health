package eg.gov.health.platform.clinicalcomms;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "critical_alerts")
public class CriticalAlert {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="from_role") private String fromRole;
  @Column(name="to_role") private String toRole;
  @Column(name="message_type") private String messageType;
  private String content; private String channel;
  @Column(name="fallback_used", nullable=false) private boolean fallbackUsed = false;
  @Column(nullable=false) private String status = "sent";
  @Column(name="sent_at") private OffsetDateTime sentAt;
  @Column(name="acknowledged_at") private OffsetDateTime acknowledgedAt;
  @Column(name="read_back_confirmed", nullable=false) private boolean readBackConfirmed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CriticalAlert() {}
  public CriticalAlert(UUID facilityId, String governorate, String patientRef, String fromRole, String toRole, String messageType,
                       String content, String channel, boolean fallbackUsed) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.fromRole=fromRole; this.toRole=toRole;
    this.messageType=messageType; this.content=content; this.channel=channel; this.fallbackUsed=fallbackUsed; this.sentAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getSentAt(){return sentAt;}
  public OffsetDateTime getAcknowledgedAt(){return acknowledgedAt;} public boolean isReadBackConfirmed(){return readBackConfirmed;}
  public void acknowledge(){ this.acknowledgedAt=OffsetDateTime.now(); this.status="acknowledged"; this.updatedAt=OffsetDateTime.now(); }
  public void readBack(){ this.readBackConfirmed=true; this.status="read_back"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
