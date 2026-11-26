package eg.gov.health.platform.iamdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "access_grants")
public class AccessGrant {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="subject_ref") private String subjectRef;
  @Column(name="role_name") private String roleName;
  private String scope; private String purpose;
  @Column(nullable=false) private boolean temporary = false;
  @Column(name="expires_at") private OffsetDateTime expiresAt;
  @Column(name="approver_ref") private String approverRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AccessGrant() {}
  public AccessGrant(UUID facilityId, String governorate, String subjectRef, String roleName, String scope, String purpose,
                       boolean temporary, OffsetDateTime expiresAt, String approverRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.subjectRef=subjectRef; this.roleName=roleName; this.scope=scope;
    this.purpose=purpose; this.temporary=temporary; this.expiresAt=expiresAt; this.approverRef=approverRef;
  }
  public UUID getId(){return id;} public boolean isTemporary(){return temporary;} public OffsetDateTime getExpiresAt(){return expiresAt;}
}
