package eg.gov.health.platform.mobiledevices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mobile_devices")
public class MobileDevice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_ref") private String deviceRef;
  @Column(name="user_ref") private String userRef;
  @Column(name="app_version") private Integer appVersion;
  @Column(name="min_supported_version") private Integer minSupportedVersion;
  @Column(name="encryption_enabled", nullable=false) private boolean encryptionEnabled = false;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MobileDevice() {}
  public MobileDevice(UUID facilityId, String governorate, String deviceRef, String userRef, Integer appVersion, Integer minSupportedVersion, boolean encryptionEnabled) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceRef=deviceRef; this.userRef=userRef; this.appVersion=appVersion;
    this.minSupportedVersion=minSupportedVersion; this.encryptionEnabled=encryptionEnabled;
  }
  public UUID getId(){return id;} public boolean isEncryptionEnabled(){return encryptionEnabled;} public String getStatus(){return status;}
  public Integer getAppVersion(){return appVersion;} public Integer getMinSupportedVersion(){return minSupportedVersion;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
