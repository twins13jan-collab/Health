package eg.gov.health.platform.institutionalrisk;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "risk_committee_decisions")
public class RiskCommitteeDecision {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="decision_ref") private String decisionRef;
  private String summary;
  @Column(name="owner_ref") private String ownerRef;
  @Column(name="due_date") private LocalDate dueDate;
  @Column(nullable=false) private boolean implemented = false;
  @Column(name="impact_measured", nullable=false) private boolean impactMeasured = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RiskCommitteeDecision() {}
  public RiskCommitteeDecision(UUID facilityId, String governorate, String decisionRef, String summary, String ownerRef, LocalDate dueDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.decisionRef=decisionRef; this.summary=summary; this.ownerRef=ownerRef; this.dueDate=dueDate;
  }
  public UUID getId(){return id;} public boolean isImplemented(){return implemented;} public boolean isImpactMeasured(){return impactMeasured;}
  public void implement(){ this.implemented=true; }
  public void measureImpact(){ this.impactMeasured=true; }
}
