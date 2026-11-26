package eg.gov.health.platform.orgchange;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "user_feedback_items")
public class UserFeedbackItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="initiative_id", nullable=false) private UUID initiativeId;
  @Column(name="user_ref") private String userRef;
  private String sentiment; private String comment;
  @Column(nullable=false) private boolean addressed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected UserFeedbackItem() {}
  public UserFeedbackItem(UUID facilityId, String governorate, UUID initiativeId, String userRef, String sentiment, String comment) {
    this.facilityId=facilityId; this.governorate=governorate; this.initiativeId=initiativeId; this.userRef=userRef; this.sentiment=sentiment; this.comment=comment;
  }
  public UUID getId(){return id;}
}
