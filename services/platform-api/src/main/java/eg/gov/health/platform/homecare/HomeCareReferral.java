package eg.gov.health.platform.homecare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "home_care_referrals")
public class HomeCareReferral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String reason;
  private String need;
  private String zone; // coarse zone only — never the exact address
  @Column(name="safety_requirements") private String safetyRequirements;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HomeCareReferral() {}
  public HomeCareReferral(UUID facilityId, String governorate, UUID patientId, String reason, String need, String zone, String safetyRequirements) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.reason=reason;
    this.need=need; this.zone=zone; this.safetyRequirements=safetyRequirements;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
