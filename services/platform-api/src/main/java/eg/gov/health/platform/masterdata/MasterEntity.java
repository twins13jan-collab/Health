package eg.gov.health.platform.masterdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "master_entities")
public class MasterEntity {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="entity_type", nullable=false) private String entityType;
  @Column(nullable=false) private String code;
  @Column(name="name_ar") private String nameAr; @Column(name="name_en") private String nameEn;
  @Column(name="owner_domain") private String ownerDomain;
  @Column(nullable=false) private int version = 1;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MasterEntity() {}
  public MasterEntity(UUID facilityId, String governorate, String entityType, String code, String nameAr, String nameEn, String ownerDomain) {
    this.facilityId=facilityId; this.governorate=governorate; this.entityType=entityType; this.code=code;
    this.nameAr=nameAr; this.nameEn=nameEn; this.ownerDomain=ownerDomain;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void deprecate(){ this.status="deprecated"; this.version++; this.updatedAt=OffsetDateTime.now(); }
}
