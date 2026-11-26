package eg.gov.health.platform.workforce;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "staff_members")
public class StaffMember {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="user_id") private String userId;
  @Column(name="full_name", nullable=false) private String fullName;
  @Column(nullable=false) private String profession; // physician, nurse, pharmacist, technician, admin, paramedic
  @Column(name="specialty") private String specialty;
  @Column(name="license_number") private String licenseNumber;
  @Column(nullable=false) private String status = "active"; // active, on_leave, suspended, departed
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StaffMember() {}
  public StaffMember(UUID facilityId, String governorate, String userId, String fullName, String profession, String specialty, String licenseNumber) {
    this.facilityId=facilityId; this.governorate=governorate; this.userId=userId; this.fullName=fullName;
    this.profession=profession; this.specialty=specialty; this.licenseNumber=licenseNumber;
  }
  public UUID getId(){return id;} public String getFullName(){return fullName;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;}
}
