package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "toxicity_assessments")
public class ToxicityAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="cycle_id") private UUID cycleId;
  @Column(nullable=false) private int grade = 0;
  private String description; private String intervention;
  @Column(name="continuation_decision") private String continuationDecision;
  @Column(name="decided_by") private String decidedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ToxicityAssessment() {}
  public ToxicityAssessment(UUID facilityId, String governorate, UUID caseId, UUID cycleId, int grade, String description, String intervention, String continuationDecision, String decidedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.cycleId=cycleId; this.grade=grade;
    this.description=description; this.intervention=intervention; this.continuationDecision=continuationDecision; this.decidedBy=decidedBy;
  }
  public UUID getId(){return id;}
}
