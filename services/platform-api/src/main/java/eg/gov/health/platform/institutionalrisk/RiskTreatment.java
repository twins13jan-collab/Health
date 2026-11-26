package eg.gov.health.platform.institutionalrisk;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "risk_treatments")
public class RiskTreatment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="risk_id", nullable=false) private UUID riskId;
  private String action;
  @Column(name="due_date") private LocalDate dueDate;
  @Column(nullable=false) private boolean done = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RiskTreatment() {}
  public RiskTreatment(UUID facilityId, String governorate, UUID riskId, String action, LocalDate dueDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.riskId=riskId; this.action=action; this.dueDate=dueDate;
  }
  public UUID getId(){return id;} public boolean isDone(){return done;}
  public void complete(){ this.done=true; }
}
