package eg.gov.health.platform.citizenportal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "outbound_messages")
public class OutboundMessage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_id", nullable=false) private UUID accountId;
  private String channel; private String template;
  @Column(nullable=false) private boolean clinical = false;
  @Column(nullable=false) private String status = "queued";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OutboundMessage() {}
  public OutboundMessage(UUID facilityId, String governorate, UUID accountId, String channel, String template, boolean clinical, String status) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountId=accountId; this.channel=channel;
    this.template=template; this.clinical=clinical; this.status=status;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
