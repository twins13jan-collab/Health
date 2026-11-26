package eg.gov.health.platform.orgchange;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "change_initiatives")
public class ChangeInitiative {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title; private String scope;
  @Column(name="impact_analysis") private String impactAnalysis;
  @Column(name="sop_updated", nullable=false) private boolean sopUpdated = false;
  @Column(name="affected_users") private Integer affectedUsers;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ChangeInitiative() {}
  public ChangeInitiative(UUID facilityId, String governorate, String title, String scope, String impactAnalysis, boolean sopUpdated, Integer affectedUsers) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.scope=scope; this.impactAnalysis=impactAnalysis;
    this.sopUpdated=sopUpdated; this.affectedUsers=affectedUsers;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getImpactAnalysis(){return impactAnalysis;}
  public boolean isSopUpdated(){return sopUpdated;} public Integer getAffectedUsers(){return affectedUsers;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
