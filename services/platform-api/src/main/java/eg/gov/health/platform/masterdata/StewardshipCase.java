package eg.gov.health.platform.masterdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "stewardship_cases")
public class StewardshipCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="change_type", nullable=false) private String changeType;
  @Column(name="target_ref", nullable=false) private String targetRef;
  @Column(nullable=false) private String justification;
  @Column(name="requested_by") private String requestedBy;
  @Column(name="decided_by") private String decidedBy;
  @Column(nullable=false) private String decision = "pending";
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StewardshipCase() {}
  public StewardshipCase(UUID facilityId, String governorate, String changeType, String targetRef, String justification, String requestedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.changeType=changeType; this.targetRef=targetRef;
    this.justification=justification; this.requestedBy=requestedBy;
  }
  public UUID getId(){return id;} public String getDecision(){return decision;} public String getChangeType(){return changeType;}
  public String getTargetRef(){return targetRef;}
  public void decide(String decision, String by){ this.decision=decision; this.decidedBy=by; this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
