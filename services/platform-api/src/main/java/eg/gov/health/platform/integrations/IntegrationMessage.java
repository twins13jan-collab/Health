package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "integration_messages")
public class IntegrationMessage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="message_id", nullable=false) private String messageId;
  @Column(name="source_system") private String sourceSystem;
  private String destination; @Column(name="content_hash") private String contentHash;
  @Column(name="trace_id") private String traceId;
  @Column(name="payload_valid", nullable=false) private boolean payloadValid = true;
  @Column(name="semantic_verified", nullable=false) private boolean semanticVerified = false;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IntegrationMessage() {}
  public IntegrationMessage(UUID facilityId, String governorate, String messageId, String sourceSystem, String destination, String contentHash, String traceId, boolean payloadValid) {
    this.facilityId=facilityId; this.governorate=governorate; this.messageId=messageId; this.sourceSystem=sourceSystem;
    this.destination=destination; this.contentHash=contentHash; this.traceId=traceId; this.payloadValid=payloadValid;
    this.status = payloadValid ? "received" : "quarantined";
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getMessageId(){return messageId;}
  public UUID getFacilityId(){return facilityId;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void verifySemantics(){ this.semanticVerified=true; this.updatedAt=OffsetDateTime.now(); }
}
