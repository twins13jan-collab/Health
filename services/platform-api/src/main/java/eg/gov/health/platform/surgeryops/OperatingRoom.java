package eg.gov.health.platform.surgeryops;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "operating_rooms")
public class OperatingRoom {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "name_ar", nullable = false) private String nameAr;
  @Column(name = "name_en") private String nameEn;
  private String specialty;
  @Column(name = "is_active", nullable = false) private boolean active = true;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OperatingRoom() {}
  public OperatingRoom(UUID facilityId, String governorate, String nameAr, String nameEn, String specialty) {
    this.facilityId = facilityId; this.governorate = governorate; this.nameAr = nameAr;
    this.nameEn = nameEn; this.specialty = specialty;
  }
  public UUID getId() { return id; }
  public UUID getFacilityId() { return facilityId; }
  public String getNameAr() { return nameAr; }
  public String getNameEn() { return nameEn; }
  public String getSpecialty() { return specialty; }
  public boolean isActive() { return active; }
  public void setActive(boolean a) { this.active = a; }
}
