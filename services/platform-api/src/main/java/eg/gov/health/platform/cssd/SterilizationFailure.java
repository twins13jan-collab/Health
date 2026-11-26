package eg.gov.health.platform.cssd;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sterilization_failures")
public class SterilizationFailure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="cycle_id") private UUID cycleId;
  @Column(name="sterilizer_code") private String sterilizerCode;
  @Column(name="failure_type") private String failureType;
  @Column(name="affected_sets") private String affectedSets;
  @Column(name="recall_issued", nullable=false) private boolean recallIssued = false;
  private String investigation; @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SterilizationFailure() {}
  public SterilizationFailure(UUID facilityId, String governorate, UUID cycleId, String sterilizerCode, String failureType, String affectedSets, boolean recallIssued, String investigation) {
    this.facilityId=facilityId; this.governorate=governorate; this.cycleId=cycleId; this.sterilizerCode=sterilizerCode;
    this.failureType=failureType; this.affectedSets=affectedSets; this.recallIssued=recallIssued; this.investigation=investigation;
  }
  public UUID getId(){return id;} public String getFailureType(){return failureType;}
  public String getSterilizerCode(){return sterilizerCode;} public boolean isRecallIssued(){return recallIssued;}
  public String getStatus(){return status;} public OffsetDateTime getCreatedAt(){return createdAt;}
}
