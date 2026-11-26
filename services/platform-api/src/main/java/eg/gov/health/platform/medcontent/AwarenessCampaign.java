package eg.gov.health.platform.medcontent;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "awareness_campaigns")
public class AwarenessCampaign {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="content_id") private UUID contentId;
  private String audience;
  @Column(name="starts_on") private LocalDate startsOn;
  @Column(name="ends_on") private LocalDate endsOn;
  @Column(name="comprehension_score") private BigDecimal comprehensionScore;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AwarenessCampaign() {}
  public AwarenessCampaign(UUID facilityId, String governorate, String name, UUID contentId, String audience, LocalDate startsOn, LocalDate endsOn) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.contentId=contentId; this.audience=audience;
    this.startsOn=startsOn; this.endsOn=endsOn;
  }
  public UUID getId(){return id;}
  public void complete(BigDecimal score){ this.comprehensionScore=score; this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
