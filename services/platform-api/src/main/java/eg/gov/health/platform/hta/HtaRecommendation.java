package eg.gov.health.platform.hta;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "hta_recommendations")
public class HtaRecommendation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  private String recommendation; private String conditions;
  @Column(name="decided_by") private String decidedBy;
  @Column(name="decided_at") private OffsetDateTime decidedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HtaRecommendation() {}
  public HtaRecommendation(UUID facilityId, String governorate, UUID assessmentId, String recommendation, String conditions, String decidedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.recommendation=recommendation;
    this.conditions=conditions; this.decidedBy=decidedBy; this.decidedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
