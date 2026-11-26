package eg.gov.health.platform.security;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "notification_records")
public class NotificationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(nullable=false) private String audience;
  @Column(nullable=false) private String decision;
  @Column(nullable=false) private String justification;
  @Column(name="decided_by") private String decidedBy;
  @Column(name="notified_at") private OffsetDateTime notifiedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NotificationRecord() {}
  public NotificationRecord(UUID facilityId, String governorate, UUID incidentId, String audience, String decision, String justification, String decidedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.audience=audience;
    this.decision=decision; this.justification=justification; this.decidedBy=decidedBy;
    this.notifiedAt = "notify".equals(decision) ? OffsetDateTime.now() : null;
  }
  public UUID getId(){return id;}
}
