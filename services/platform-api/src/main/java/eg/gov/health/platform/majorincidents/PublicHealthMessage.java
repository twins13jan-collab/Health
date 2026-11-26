package eg.gov.health.platform.majorincidents;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "public_health_messages")
public class PublicHealthMessage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id") private UUID planId;
  private String message;
  @Column(name="source_verified", nullable=false) private boolean sourceVerified = false;
  @Column(name="rumor_refutation", nullable=false) private boolean rumorRefutation = false;
  @Column(nullable=false) private boolean published = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PublicHealthMessage() {}
  public PublicHealthMessage(UUID facilityId, String governorate, UUID planId, String message, boolean sourceVerified, boolean rumorRefutation) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.message=message; this.sourceVerified=sourceVerified; this.rumorRefutation=rumorRefutation;
  }
  public UUID getId(){return id;} public boolean isSourceVerified(){return sourceVerified;}
  public void publish(){ this.published=true; }
}
