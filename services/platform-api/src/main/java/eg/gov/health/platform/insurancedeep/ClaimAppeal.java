package eg.gov.health.platform.insurancedeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "claim_appeals")
public class ClaimAppeal {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="claim_id", nullable=false) private UUID claimId;
  private String grounds;
  @Column(nullable=false) private boolean decided = false;
  private String outcome;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClaimAppeal() {}
  public ClaimAppeal(UUID facilityId, String governorate, UUID claimId, String grounds) {
    this.facilityId=facilityId; this.governorate=governorate; this.claimId=claimId; this.grounds=grounds;
  }
  public UUID getId(){return id;}
  public UUID getClaimId(){return claimId;} public boolean isDecided(){return decided;}
  public String getGrounds(){return grounds;} public String getOutcome(){return outcome;}
  public OffsetDateTime getCreatedAt(){return createdAt;}
  public void decide(String outcome){ this.decided=true; this.outcome=outcome; }
}
