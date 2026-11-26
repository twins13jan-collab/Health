package eg.gov.health.platform.offline;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "offline_work_packages")
public class OfflineWorkPackage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_id", nullable=false) private UUID deviceId;
  @Column(name="user_ref") private String userRef;
  private String scope; @Column(name="allowed_data") private String allowedData;
  @Column(name="reference_data_version", nullable=false) private int referenceDataVersion = 1;
  @Column(name="valid_from") private OffsetDateTime validFrom = OffsetDateTime.now();
  @Column(name="valid_until", nullable=false) private OffsetDateTime validUntil;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected OfflineWorkPackage() {}
  public OfflineWorkPackage(UUID facilityId, String governorate, UUID deviceId, String userRef, String scope, String allowedData, int referenceDataVersion, OffsetDateTime validUntil) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceId=deviceId; this.userRef=userRef; this.scope=scope;
    this.allowedData=allowedData; this.referenceDataVersion=referenceDataVersion; this.validUntil=validUntil;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getDeviceId(){return deviceId;}
  public String getStatus(){return status;} public OffsetDateTime getValidUntil(){return validUntil;} public String getScope(){return scope;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
