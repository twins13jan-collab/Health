package eg.gov.health.platform.clinics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinics")
public class Clinic {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "name_ar", nullable = false) private String nameAr;
  @Column(name = "name_en") private String nameEn;
  private String specialty; private String room;
  @Column(name = "is_active") private boolean active = true;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected Clinic() {}
  public Clinic(UUID facilityId, String governorate, String nameAr, String nameEn, String specialty, String room) {
    this.facilityId = facilityId; this.governorate = governorate; this.nameAr = nameAr;
    this.nameEn = nameEn; this.specialty = specialty; this.room = room;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; }
  public String getGovernorate() { return governorate; } public String getNameAr() { return nameAr; }
  public String getNameEn() { return nameEn; } public String getSpecialty() { return specialty; }
  public String getRoom() { return room; } public boolean isActive() { return active; }
  public void setActive(boolean a) { this.active = a; }
}
