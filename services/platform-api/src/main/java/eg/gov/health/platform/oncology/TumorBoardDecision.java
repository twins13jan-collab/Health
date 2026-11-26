package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "tumor_board_decisions")
public class TumorBoardDecision {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  private String decision; private String participants;
  @Column(name="decided_at") private OffsetDateTime decidedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TumorBoardDecision() {}
  public TumorBoardDecision(UUID facilityId, String governorate, UUID caseId, String decision, String participants) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.decision=decision; this.participants=participants; this.decidedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
