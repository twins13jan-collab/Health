package eg.gov.health.platform.toxicology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "referral_outcomes")
public class ReferralOutcome {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="receiving_facility", nullable=false) private String receivingFacility;
  private String transport; private String outcome;
  @Column(name="follow_up") private String followUp;
  @Column(nullable=false) private String status = "initiated";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ReferralOutcome() {}
  public ReferralOutcome(UUID facilityId, String governorate, UUID caseId, String receivingFacility, String transport) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId;
    this.receivingFacility=receivingFacility; this.transport=transport;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void recordOutcome(String outcome, String followUp){ this.outcome=outcome; this.followUp=followUp; this.status="outcome_recorded"; this.updatedAt=OffsetDateTime.now(); }
}
