package eg.gov.health.platform.security;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "incident_actions")
public class IncidentAction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(nullable=false) private String phase;
  @Column(nullable=false) private String action;
  private String owner;
  @Column(name="clean_backup_verified", nullable=false) private boolean cleanBackupVerified = false;
  @Column(name="done_at") private OffsetDateTime doneAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IncidentAction() {}
  public IncidentAction(UUID facilityId, String governorate, UUID incidentId, String phase, String action, String owner, boolean cleanBackupVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.phase=phase;
    this.action=action; this.owner=owner; this.cleanBackupVerified=cleanBackupVerified;
  }
  public UUID getId(){return id;} public String getPhase(){return phase;}
}
