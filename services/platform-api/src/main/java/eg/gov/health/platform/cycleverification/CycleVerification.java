package eg.gov.health.platform.cycleverification;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cycle_verifications")
public class CycleVerification {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="cycle_ref") private String cycleRef;
  @Column(name="instance_ref") private String instanceRef;
  @Column(name="request_done", nullable=false) private boolean requestDone = false;
  @Column(name="triage_done", nullable=false) private boolean triageDone = false;
  @Column(name="approval_done", nullable=false) private boolean approvalDone = false;
  @Column(name="execution_done", nullable=false) private boolean executionDone = false;
  @Column(name="result_done", nullable=false) private boolean resultDone = false;
  @Column(name="followup_done", nullable=false) private boolean followupDone = false;
  @Column(name="evidence_refs") private String evidenceRefs;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CycleVerification() {}
  public CycleVerification(UUID facilityId, String governorate, String cycleRef, String instanceRef, boolean requestDone, boolean triageDone,
                             boolean approvalDone, boolean executionDone, boolean resultDone, boolean followupDone, String evidenceRefs) {
    this.facilityId=facilityId; this.governorate=governorate; this.cycleRef=cycleRef; this.instanceRef=instanceRef;
    this.requestDone=requestDone; this.triageDone=triageDone; this.approvalDone=approvalDone; this.executionDone=executionDone;
    this.resultDone=resultDone; this.followupDone=followupDone; this.evidenceRefs=evidenceRefs;
  }
  public UUID getId(){return id;} public boolean isRequestDone(){return requestDone;} public boolean isTriageDone(){return triageDone;}
  public boolean isApprovalDone(){return approvalDone;} public boolean isExecutionDone(){return executionDone;}
  public boolean isResultDone(){return resultDone;} public boolean isFollowupDone(){return followupDone;}
  public String getEvidenceRefs(){return evidenceRefs;}
}
