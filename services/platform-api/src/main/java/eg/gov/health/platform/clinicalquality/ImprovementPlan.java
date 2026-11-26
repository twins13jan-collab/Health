package eg.gov.health.platform.clinicalquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "improvement_plans")
public class ImprovementPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title;
  @Column(name="actions_total") private Integer actionsTotal;
  @Column(name="actions_done") private Integer actionsDone;
  @Column(name="effectiveness_verified", nullable=false) private boolean effectivenessVerified = false;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ImprovementPlan() {}
  public ImprovementPlan(UUID facilityId, String governorate, String title, Integer actionsTotal, Integer actionsDone, boolean effectivenessVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.actionsTotal=actionsTotal;
    this.actionsDone=actionsDone; this.effectivenessVerified=effectivenessVerified;
  }
  public UUID getId(){return id;} public Integer getActionsTotal(){return actionsTotal;} public Integer getActionsDone(){return actionsDone;}
  public boolean isEffectivenessVerified(){return effectivenessVerified;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
