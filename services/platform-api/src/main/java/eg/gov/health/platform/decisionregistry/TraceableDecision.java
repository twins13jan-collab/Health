package eg.gov.health.platform.decisionregistry;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "traceable_decisions")
public class TraceableDecision {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="decision_ref") private String decisionRef;
  private String title;
  @Column(name="decision_maker") private String decisionMaker;
  @Column(name="authority_basis") private String authorityBasis;
  @Column(name="context_summary") private String contextSummary;
  @Column(name="alternatives_considered") private String alternativesConsidered;
  private String rationale;
  @Column(name="linked_approval_ref") private String linkedApprovalRef;
  @Column(name="linked_protocol_ref") private String linkedProtocolRef;
  @Column(name="committee_decision", nullable=false) private boolean committeeDecision = false;
  @Column(name="coi_clear", nullable=false) private boolean coiClear = false;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TraceableDecision() {}
  public TraceableDecision(UUID facilityId, String governorate, String decisionRef, String title, String decisionMaker, String authorityBasis,
                           String contextSummary, String alternativesConsidered, String rationale, String linkedApprovalRef, String linkedProtocolRef,
                           boolean committeeDecision, boolean coiClear, UUID supersedesId) {
    this.facilityId=facilityId; this.governorate=governorate; this.decisionRef=decisionRef; this.title=title; this.decisionMaker=decisionMaker;
    this.authorityBasis=authorityBasis; this.contextSummary=contextSummary; this.alternativesConsidered=alternativesConsidered; this.rationale=rationale;
    this.linkedApprovalRef=linkedApprovalRef; this.linkedProtocolRef=linkedProtocolRef; this.committeeDecision=committeeDecision; this.coiClear=coiClear;
    this.supersedesId=supersedesId;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
