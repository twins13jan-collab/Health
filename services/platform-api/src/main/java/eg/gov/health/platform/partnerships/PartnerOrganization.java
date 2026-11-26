package eg.gov.health.platform.partnerships;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "partner_organizations")
public class PartnerOrganization {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="partner_type") private String partnerType;
  @Column(name="coi_declared", nullable=false) private boolean coiDeclared = false;
  @Column(name="ownership_note") private String ownershipNote;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PartnerOrganization() {}
  public PartnerOrganization(UUID facilityId, String governorate, String name, String partnerType, boolean coiDeclared, String ownershipNote) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.partnerType=partnerType;
    this.coiDeclared=coiDeclared; this.ownershipNote=ownershipNote;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
