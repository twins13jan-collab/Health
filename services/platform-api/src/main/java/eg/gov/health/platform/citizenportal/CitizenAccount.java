package eg.gov.health.platform.citizenportal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "citizen_accounts")
public class CitizenAccount {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="citizen_ref", nullable=false) private String citizenRef;
  @Column(name="full_name") private String fullName;
  @Column(name="identity_verified", nullable=false) private boolean identityVerified = false;
  @Column(name="verification_method") private String verificationMethod;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CitizenAccount() {}
  public CitizenAccount(UUID facilityId, String governorate, String citizenRef, String fullName) {
    this.facilityId=facilityId; this.governorate=governorate; this.citizenRef=citizenRef; this.fullName=fullName;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public boolean isIdentityVerified(){return identityVerified;}
  public String getStatus(){return status;}
  public void verify(String method){ this.identityVerified=true; this.verificationMethod=method; this.updatedAt=OffsetDateTime.now(); }
}
