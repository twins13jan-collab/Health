package eg.gov.health.platform.facility;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "facilities")
public class Facility {
  @Id @GeneratedValue private UUID id;
  @Column(name = "name_ar", nullable = false) private String nameAr;
  @Column(name = "name_en") private String nameEn;
  @Column(nullable = false) private String type;   // hospital, health_unit, family_center, ...
  @Column(nullable = false) private String level;  // primary, secondary, tertiary, quaternary
  @Column(nullable = false) private String governorate;
  private String directorate; private String city;
  @Column(name = "ownership") private String ownership; // moh, university, insurance, military, police, private, ngo, sectoral
  @Column(name = "bed_capacity") private Integer bedCapacity;
  @Column(name = "license_no") private String licenseNo;
  @Column(name = "license_authority") private String licenseAuthority;
  @Column(name = "license_expiry") private java.time.LocalDate licenseExpiry;
  @Column(nullable = false) private String status = "active";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Facility() {}
  public Facility(String nameAr, String nameEn, String type, String level, String governorate, String directorate, String city, String ownership, Integer bedCapacity) {
    this.nameAr = nameAr; this.nameEn = nameEn; this.type = type; this.level = level; this.governorate = governorate;
    this.directorate = directorate; this.city = city; this.ownership = ownership; this.bedCapacity = bedCapacity;
  }
  public UUID getId() { return id; } public String getNameAr() { return nameAr; } public String getNameEn() { return nameEn; }
  public String getType() { return type; } public String getLevel() { return level; } public String getGovernorate() { return governorate; }
  public String getDirectorate() { return directorate; } public String getCity() { return city; }
  public String getOwnership() { return ownership; } public Integer getBedCapacity() { return bedCapacity; } public String getStatus() { return status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public String getLicenseNo() { return licenseNo; } public String getLicenseAuthority() { return licenseAuthority; }
  public java.time.LocalDate getLicenseExpiry() { return licenseExpiry; }
  public void setLicense(String no, String authority, java.time.LocalDate expiry) {
    this.licenseNo = no; this.licenseAuthority = authority; this.licenseExpiry = expiry; this.updatedAt = OffsetDateTime.now();
  }
  public void setStatus(String s) { this.status = s; this.updatedAt = OffsetDateTime.now(); }
}
