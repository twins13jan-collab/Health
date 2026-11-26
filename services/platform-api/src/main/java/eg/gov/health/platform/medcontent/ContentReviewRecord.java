package eg.gov.health.platform.medcontent;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "content_review_records")
public class ContentReviewRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="content_id", nullable=false) private UUID contentId;
  private String reviewer; private String decision; private String notes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ContentReviewRecord() {}
  public ContentReviewRecord(UUID facilityId, String governorate, UUID contentId, String reviewer, String decision, String notes) {
    this.facilityId=facilityId; this.governorate=governorate; this.contentId=contentId; this.reviewer=reviewer; this.decision=decision; this.notes=notes;
  }
  public UUID getId(){return id;}
}
