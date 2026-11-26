package eg.gov.health.platform.hrdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "employee_records")
public class EmployeeRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="staff_ref") private String staffRef;
  private String role;
  @Column(nullable=false) private boolean clinical = false;
  @Column(name="licence_valid", nullable=false) private boolean licenceValid = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EmployeeRecord() {}
  public EmployeeRecord(UUID facilityId, String governorate, String staffRef, String role, boolean clinical, boolean licenceValid) {
    this.facilityId=facilityId; this.governorate=governorate; this.staffRef=staffRef; this.role=role; this.clinical=clinical; this.licenceValid=licenceValid;
  }
  public UUID getId(){return id;} public boolean isClinical(){return clinical;} public boolean isLicenceValid(){return licenceValid;}
  public String getStaffRef(){return staffRef;} public String getRole(){return role;}
  public UUID getFacilityId(){return facilityId;} public OffsetDateTime getCreatedAt(){return createdAt;}
}
