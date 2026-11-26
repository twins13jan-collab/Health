package eg.gov.health.platform.rehab;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "rehab_referrals")
public class RehabReferral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String reason;
  private String diagnosis; private String restrictions;
  @Column(name="referring_physician") private String referringPhysician;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RehabReferral() {}
  public RehabReferral(UUID facilityId, String governorate, UUID patientId, String reason, String diagnosis, String restrictions, String referringPhysician) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.reason=reason;
    this.diagnosis=diagnosis; this.restrictions=restrictions; this.referringPhysician=referringPhysician;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
