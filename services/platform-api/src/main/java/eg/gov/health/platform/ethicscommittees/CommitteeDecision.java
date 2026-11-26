package eg.gov.health.platform.ethicscommittees;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "committee_decisions")
public class CommitteeDecision {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  private String decision;
  @Column(name="votes_present") private Integer votesPresent;
  @Column(name="voter_refs") private String voterRefs;
  @Column(name="recused_members") private String recusedMembers;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CommitteeDecision() {}
  public CommitteeDecision(UUID facilityId, String governorate, UUID caseId, String decision, Integer votesPresent, String voterRefs, String recusedMembers) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.decision=decision; this.votesPresent=votesPresent;
    this.voterRefs=voterRefs; this.recusedMembers=recusedMembers;
  }
  public UUID getId(){return id;}
}
