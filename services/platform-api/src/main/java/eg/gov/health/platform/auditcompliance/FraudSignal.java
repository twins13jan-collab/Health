package eg.gov.health.platform.auditcompliance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "fraud_signals")
public class FraudSignal {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="signal_ref") private String signalRef;
  @Column(name="human_reviewed", nullable=false) private boolean humanReviewed = false;
  @Column(name="human_outcome") private String humanOutcome;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FraudSignal() {}
  public FraudSignal(UUID facilityId, String governorate, String signalRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.signalRef=signalRef;
  }
  public UUID getId(){return id;} public boolean isHumanReviewed(){return humanReviewed;} public String getHumanOutcome(){return humanOutcome;}
  public void decide(String outcome){ this.humanReviewed=true; this.humanOutcome=outcome; this.updatedAt=OffsetDateTime.now(); }
}
