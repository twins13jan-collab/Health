package eg.gov.health.platform.offline;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "device_trust")
public class DeviceTrust {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_code", nullable=false) private String deviceCode;
  @Column(name="user_ref") private String userRef;
  @Column(name="encryption_enabled", nullable=false) private boolean encryptionEnabled = false;
  @Column(nullable=false) private String status = "trusted";
  @Column(name="last_verified_at") private OffsetDateTime lastVerifiedAt = OffsetDateTime.now();
  @Column(name="revoked_by") private String revokedBy;
  @Column(name="revoke_reason") private String revokeReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeviceTrust() {}
  public DeviceTrust(UUID facilityId, String governorate, String deviceCode, String userRef, boolean encryptionEnabled) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceCode=deviceCode; this.userRef=userRef;
    this.encryptionEnabled=encryptionEnabled;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public boolean isEncryptionEnabled(){return encryptionEnabled;}
  public void revoke(String by, String reason){ this.status="revoked"; this.revokedBy=by; this.revokeReason=reason; this.updatedAt=OffsetDateTime.now(); }
}
