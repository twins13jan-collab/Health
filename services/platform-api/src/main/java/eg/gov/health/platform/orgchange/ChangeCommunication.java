package eg.gov.health.platform.orgchange;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "change_communications")
public class ChangeCommunication {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="initiative_id", nullable=false) private UUID initiativeId;
  private String audience; private String channel;
  @Column(name="sent_at") private OffsetDateTime sentAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ChangeCommunication() {}
  public ChangeCommunication(UUID facilityId, String governorate, UUID initiativeId, String audience, String channel) {
    this.facilityId=facilityId; this.governorate=governorate; this.initiativeId=initiativeId; this.audience=audience; this.channel=channel;
    this.sentAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
