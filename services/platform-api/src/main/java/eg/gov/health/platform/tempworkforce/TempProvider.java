package eg.gov.health.platform.tempworkforce;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "temp_providers")
public class TempProvider {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="provider_ref") private String providerRef;
  private String profession;
  @Column(name="license_no") private String licenseNo;
  @Column(name="license_valid_until") private LocalDate licenseValidUntil;
  @Column(name="identity_verified", nullable=false) private boolean identityVerified = false;
  @Column(name="qualifications_verified", nullable=false) private boolean qualificationsVerified = false;
  @Column(name="practice_scope") private String practiceScope;
  @Column(nullable=false) private String status = "pending";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TempProvider() {}
  public TempProvider(UUID facilityId, String governorate, String providerRef, String profession, String licenseNo, LocalDate licenseValidUntil,
                      boolean identityVerified, boolean qualificationsVerified, String practiceScope) {
    this.facilityId=facilityId; this.governorate=governorate; this.providerRef=providerRef; this.profession=profession; this.licenseNo=licenseNo;
    this.licenseValidUntil=licenseValidUntil; this.identityVerified=identityVerified; this.qualificationsVerified=qualificationsVerified;
    this.practiceScope=practiceScope; if (identityVerified && qualificationsVerified) this.status="verified";
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public LocalDate getLicenseValidUntil(){return licenseValidUntil;}
  public boolean isIdentityVerified(){return identityVerified;} public boolean isQualificationsVerified(){return qualificationsVerified;}
  public String getPracticeScope(){return practiceScope;}
}
