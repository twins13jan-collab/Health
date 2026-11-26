package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "acknowledgements")
public class Acknowledgement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="message_ref", nullable=false) private UUID messageRef;
  @Column(name="ack_type", nullable=false) private String ackType;
  @Column(name="semantic_verified", nullable=false) private boolean semanticVerified = false;
  @Column(name="received_at") private OffsetDateTime receivedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected Acknowledgement() {}
  public Acknowledgement(UUID facilityId, String governorate, UUID messageRef, String ackType, boolean semanticVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.messageRef=messageRef; this.ackType=ackType; this.semanticVerified=semanticVerified;
  }
  public UUID getId(){return id;}
}
