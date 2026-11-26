package eg.gov.health.platform.preventive;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "screening_results")
public class ScreeningResult {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="invitation_id", nullable=false) private UUID invitationId;
  private String result;
  @Column(name="confirmatory_done", nullable=false) private boolean confirmatoryDone = false;
  @Column(name="referral_created", nullable=false) private boolean referralCreated = false;
  @Column(name="pathway_closed", nullable=false) private boolean pathwayClosed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ScreeningResult() {}
  public ScreeningResult(UUID facilityId, String governorate, UUID invitationId, String result) {
    this.facilityId=facilityId; this.governorate=governorate; this.invitationId=invitationId; this.result=result;
  }
  public UUID getId(){return id;} public String getResult(){return result;}
  public boolean isConfirmatoryDone(){return confirmatoryDone;} public boolean isReferralCreated(){return referralCreated;}
  public void confirm(){ this.confirmatoryDone=true; this.updatedAt=OffsetDateTime.now(); }
  public void refer(){ this.referralCreated=true; this.updatedAt=OffsetDateTime.now(); }
  public void closePathway(){ this.pathwayClosed=true; this.updatedAt=OffsetDateTime.now(); }
}
