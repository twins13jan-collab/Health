package eg.gov.health.platform.servicedesk;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "support_tickets")
public class SupportTicket {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String reporter;
  @Column(name="service_ref") private String serviceRef;
  @Column(name="device_ref") private String deviceRef;
  private String category; private String severity; private String track;
  private String description;
  @Column(name="assigned_to") private String assignedTo;
  @Column(name="first_response_at") private OffsetDateTime firstResponseAt;
  @Column(name="resolved_at") private OffsetDateTime resolvedAt;
  private String resolution;
  @Column(name="satisfaction_score") private Integer satisfactionScore;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SupportTicket() {}
  public SupportTicket(UUID facilityId, String governorate, String reporter, String serviceRef, String deviceRef, String category,
                       String severity, String description, OffsetDateTime createdAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.reporter=reporter; this.serviceRef=serviceRef; this.deviceRef=deviceRef;
    this.category=category; this.severity=severity; this.description=description; this.track=ServiceDeskRules.routeTrack(category);
    if (createdAt != null) this.createdAt=createdAt;
  }
  public UUID getId(){return id;} public String getTrack(){return track;} public String getStatus(){return status;}
  public String getSeverity(){return severity;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getFirstResponseAt(){return firstResponseAt;}
  public void respond(OffsetDateTime at){ if (this.firstResponseAt==null) this.firstResponseAt=at; this.status="assigned"; this.updatedAt=OffsetDateTime.now(); }
  public void escalate(){ this.status="escalated"; this.updatedAt=OffsetDateTime.now(); }
  public void resolve(String resolution){ this.resolution=resolution; this.resolvedAt=OffsetDateTime.now(); this.status="resolved"; this.updatedAt=OffsetDateTime.now(); }
  public void rate(int score){ this.satisfactionScore=score; this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
