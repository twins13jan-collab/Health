package eg.gov.health.platform.adt;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "wards")
public class Ward {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="name_ar", nullable=false) private String nameAr;
  @Column(name="name_en") private String nameEn;
  @Column(name="ward_type", nullable=false) private String wardType = "general";
  @Column(name="total_beds", nullable=false) private int totalBeds = 0;
  @Column(name="is_active", nullable=false) private boolean active = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Ward() {}
  public Ward(UUID facilityId, String governorate, String nameAr, String nameEn, String wardType, int totalBeds) {
    this.facilityId=facilityId; this.governorate=governorate; this.nameAr=nameAr; this.nameEn=nameEn;
    this.wardType=wardType==null?"general":wardType; this.totalBeds=Math.max(0,totalBeds);
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public String getNameAr(){return nameAr;} public String getNameEn(){return nameEn;} public String getWardType(){return wardType;}
  public int getTotalBeds(){return totalBeds;} public boolean isActive(){return active;}
  public void update(String nameAr, String nameEn, String wardType, int totalBeds) {
    if (nameAr!=null) this.nameAr=nameAr; if (nameEn!=null) this.nameEn=nameEn;
    if (wardType!=null) this.wardType=wardType; this.totalBeds=Math.max(0,totalBeds); this.updatedAt=OffsetDateTime.now();
  }
  public void deactivate(){ this.active=false; this.updatedAt=OffsetDateTime.now(); }
}
