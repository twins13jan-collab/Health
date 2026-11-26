package eg.gov.health.platform.specialpopulations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mobile_population_encounters")
public class MobilePopulationEncounter {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="point_id", nullable=false) private UUID pointId;
  @Column(name="person_ref") private String personRef;
  @Column(name="barrier_flag") private String barrierFlag;
  @Column(name="care_provided", nullable=false) private boolean careProvided = false;
  @Column(name="denial_review_required", nullable=false) private boolean denialReviewRequired = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MobilePopulationEncounter() {}
  public MobilePopulationEncounter(UUID facilityId, String governorate, UUID pointId, String personRef, String barrierFlag, boolean careProvided, boolean denialReviewRequired) {
    this.facilityId=facilityId; this.governorate=governorate; this.pointId=pointId; this.personRef=personRef; this.barrierFlag=barrierFlag;
    this.careProvided=careProvided; this.denialReviewRequired=denialReviewRequired;
  }
  public UUID getId(){return id;}
}
