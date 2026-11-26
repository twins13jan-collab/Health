package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "delivery_attempts")
public class DeliveryAttempt {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="message_ref", nullable=false) private UUID messageRef;
  @Column(name="attempt_no", nullable=false) private int attemptNo;
  private String result; @Column(name="error_code") private String errorCode;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DeliveryAttempt() {}
  public DeliveryAttempt(UUID facilityId, String governorate, UUID messageRef, int attemptNo, String result, String errorCode) {
    this.facilityId=facilityId; this.governorate=governorate; this.messageRef=messageRef; this.attemptNo=attemptNo;
    this.result=result; this.errorCode=errorCode;
  }
  public UUID getId(){return id;} public int getAttemptNo(){return attemptNo;}
}
