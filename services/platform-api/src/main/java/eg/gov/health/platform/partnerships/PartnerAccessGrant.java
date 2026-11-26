package eg.gov.health.platform.partnerships;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "partner_access_grants")
public class PartnerAccessGrant {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="partner_id", nullable=false) private UUID partnerId;
  @Column(name="system_ref") private String systemRef;
  @Column(name="access_level") private String accessLevel;
  private String purpose;
  @Column(name="granted_by") private String grantedBy;
  @Column(name="expires_at") private OffsetDateTime expiresAt;
  @Column(name="revoked_at") private OffsetDateTime revokedAt;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PartnerAccessGrant() {}
  public PartnerAccessGrant(UUID facilityId, String governorate, UUID partnerId, String systemRef, String accessLevel, String purpose, String grantedBy, OffsetDateTime expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.partnerId=partnerId; this.systemRef=systemRef; this.accessLevel=accessLevel;
    this.purpose=purpose; this.grantedBy=grantedBy; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void revoke(){ this.status="revoked"; this.revokedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
