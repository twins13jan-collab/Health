package eg.gov.health.platform.clinicalai;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ai_human_reviews")
public class AiHumanReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="inference_id", nullable=false) private UUID inferenceId;
  private String reviewer; @Column(nullable=false) private String decision;
  private String reason; @Column(name="final_decision") private String finalDecision;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AiHumanReview() {}
  public AiHumanReview(UUID facilityId, String governorate, UUID inferenceId, String reviewer, String decision, String reason, String finalDecision) {
    this.facilityId=facilityId; this.governorate=governorate; this.inferenceId=inferenceId; this.reviewer=reviewer;
    this.decision=decision; this.reason=reason; this.finalDecision=finalDecision;
  }
  public UUID getId(){return id;} public String getDecision(){return decision;}
}
