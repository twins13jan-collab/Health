package eg.gov.health.platform.facilitysafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "sensitive_access_grants")
public class SensitiveAccessGrant {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String person; @Column(name="person_type") private String personType;
  @Column(name="sensitive_area") private String sensitiveArea;
  @Column(name="escort_required", nullable=false) private boolean escortRequired = true;
  @Column(name="escorted_by") private String escortedBy;
  @Column(name="approved_by") private String approvedBy;
  @Column(name="valid_from") private OffsetDateTime validFrom;
  @Column(name="valid_until") private OffsetDateTime validUntil;
  @Column(nullable=false) private String status = "granted";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SensitiveAccessGrant() {}
  public SensitiveAccessGrant(UUID facilityId, String governorate, String person, String personType, String sensitiveArea, String escortedBy, String approvedBy, OffsetDateTime validFrom, OffsetDateTime validUntil) {
    this.facilityId=facilityId; this.governorate=governorate; this.person=person; this.personType=personType; this.sensitiveArea=sensitiveArea;
    this.escortedBy=escortedBy; this.approvedBy=approvedBy; this.validFrom=validFrom; this.validUntil=validUntil;
    this.escortRequired = !"staff".equals(personType);
  }
  public UUID getId(){return id;} public String getPersonType(){return personType;} public String getEscortedBy(){return escortedBy;}
  public String getApprovedBy(){return approvedBy;} public OffsetDateTime getValidFrom(){return validFrom;} public OffsetDateTime getValidUntil(){return validUntil;}
  public void revoke(){ this.status="revoked"; this.updatedAt=OffsetDateTime.now(); }
}
