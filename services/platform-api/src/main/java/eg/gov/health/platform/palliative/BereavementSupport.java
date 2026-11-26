package eg.gov.health.platform.palliative;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "bereavement_supports")
public class BereavementSupport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="family_contact") private String familyContact;
  private String request; private String referral;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected BereavementSupport() {}
  public BereavementSupport(UUID facilityId, String governorate, UUID patientId, String familyContact, String request, String referral) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId;
    this.familyContact=familyContact; this.request=request; this.referral=referral;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
