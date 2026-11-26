package eg.gov.health.platform.patient;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patients")
public class Patient {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id") private UUID facilityId;
  private String governorate;
  @Column(name = "national_id", unique = true) private String nationalId;
  @Column(name = "full_name", nullable = false) private String fullName;
  @Column(name = "birth_date") private LocalDate birthDate;
  private String gender; private String phone; private String address;
  @Column(name = "identity_status", nullable = false) private String identityStatus = "verified"; // verified, temporary, unknown
  @Column(name = "merged_into") private UUID mergedInto;
  @Column(nullable = false) private String status = "active";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Patient() {}
  public Patient(UUID facilityId, String governorate, String nationalId, String fullName, LocalDate birthDate, String gender, String phone, String address, String identityStatus) {
    this.facilityId = facilityId; this.governorate = governorate; this.nationalId = nationalId; this.fullName = fullName;
    this.birthDate = birthDate; this.gender = gender; this.phone = phone; this.address = address;
    this.identityStatus = identityStatus == null ? "verified" : identityStatus;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public String getNationalId() { return nationalId; } public String getFullName() { return fullName; }
  public LocalDate getBirthDate() { return birthDate; } public String getGender() { return gender; }
  public String getPhone() { return phone; } public String getAddress() { return address; }
  public String getIdentityStatus() { return identityStatus; } public UUID getMergedInto() { return mergedInto; }
  public String getStatus() { return status; }
  public void setStatus(String s) { this.status = s; }
  public void markMergedInto(UUID target) { this.mergedInto = target; this.status = "merged"; this.updatedAt = OffsetDateTime.now(); }
}
