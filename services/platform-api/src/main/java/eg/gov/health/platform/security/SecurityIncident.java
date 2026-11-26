package eg.gov.health.platform.security;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "security_incidents")
public class SecurityIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String type;
  private String systems; private String assets;
  @Column(name="discovered_at") private OffsetDateTime discoveredAt = OffsetDateTime.now();
  private String source; private String impact; private String severity;
  @Column(name="sessions_revoked", nullable=false) private boolean sessionsRevoked = false;
  @Column(name="clean_backup_verified", nullable=false) private boolean cleanBackupVerified = false;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SecurityIncident() {}
  public SecurityIncident(UUID facilityId, String governorate, String type, String systems, String assets, String source, String impact, String severity) {
    this.facilityId=facilityId; this.governorate=governorate; this.type=type; this.systems=systems; this.assets=assets;
    this.source=source; this.impact=impact; this.severity=severity;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getType(){return type;}
  public String getStatus(){return status;} public boolean isSessionsRevoked(){return sessionsRevoked;} public boolean isCleanBackupVerified(){return cleanBackupVerified;}
  public void revokeSessions(){ this.sessionsRevoked=true; this.updatedAt=OffsetDateTime.now(); }
  public void verifyCleanBackup(){ this.cleanBackupVerified=true; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
