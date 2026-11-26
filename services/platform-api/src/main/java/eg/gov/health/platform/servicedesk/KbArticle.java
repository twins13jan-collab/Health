package eg.gov.health.platform.servicedesk;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "kb_articles")
public class KbArticle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title; private String category; private String solution;
  @Column(nullable=false) private boolean published = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected KbArticle() {}
  public KbArticle(UUID facilityId, String governorate, String title, String category, String solution, boolean published) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.category=category; this.solution=solution; this.published=published;
  }
  public UUID getId(){return id;}
}
