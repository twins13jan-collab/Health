package eg.gov.health.platform.pathology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pathology_second_opinions")
public class PathologySecondOpinion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="report_id", nullable=false) private UUID reportId;
  @Column(nullable=false) private String pathologist;
  @Column(nullable=false) private String opinion;
  @Column(name="review_decision") private String reviewDecision;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PathologySecondOpinion() {}
  public PathologySecondOpinion(UUID facilityId, String governorate, UUID reportId, String pathologist, String opinion, String reviewDecision) {
    this.facilityId=facilityId; this.governorate=governorate; this.reportId=reportId; this.pathologist=pathologist;
    this.opinion=opinion; this.reviewDecision=reviewDecision;
  }
  public UUID getId(){return id;}
}
