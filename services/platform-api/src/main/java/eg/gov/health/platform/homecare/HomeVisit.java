package eg.gov.health.platform.homecare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "home_visits")
public class HomeVisit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="scheduled_at", nullable=false) private OffsetDateTime scheduledAt;
  private String team;
  @Column(name="arrived_at") private OffsetDateTime arrivedAt;
  @Column(name="left_at") private OffsetDateTime leftAt;
  private String services; private String outcome;
  @Column(name="access_notes") private String accessNotes;
  @Column(name="follow_up_required", nullable=false) private boolean followUpRequired = false;
  @Column(name="escalation_triggered", nullable=false) private boolean escalationTriggered = false;
  @Column(nullable=false) private String status = "scheduled";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HomeVisit() {}
  public HomeVisit(UUID facilityId, String governorate, UUID planId, OffsetDateTime scheduledAt, String team) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.scheduledAt=scheduledAt; this.team=team;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getArrivedAt(){return arrivedAt;}
  public boolean isEscalationTriggered(){return escalationTriggered;} public boolean isFollowUpRequired(){return followUpRequired;}
  public void arrive(){ this.arrivedAt=OffsetDateTime.now(); this.status="in_progress"; this.updatedAt=OffsetDateTime.now(); }
  public void complete(OffsetDateTime leftAt, String services, String outcome, boolean escalate){
    this.leftAt=leftAt; this.services=services; this.outcome=outcome; this.escalationTriggered=escalate;
    this.status="completed"; this.updatedAt=OffsetDateTime.now();
  }
  public void noAccess(String notes){ this.accessNotes=notes; this.status="missed_no_access"; this.followUpRequired=true; this.updatedAt=OffsetDateTime.now(); }
}
