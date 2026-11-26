package eg.gov.health.platform.bloodbank;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "blood_units")
public class BloodUnit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="unit_code", nullable=false, unique=true) private String unitCode;
  @Column(name="blood_group", nullable=false) private String bloodGroup;
  @Column(nullable=false) private String component = "RBC"; // RBC, PLASMA, PLATELETS, WHOLE
  @Column(name="donated_at") private LocalDate donatedAt;
  @Column(name="expires_at") private LocalDate expiresAt;
  @Column(nullable=false) private String status = "available"; // available, reserved, issued, transfused, expired, discarded
  @Column(name="reserved_for_patient") private UUID reservedForPatient;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected BloodUnit() {}
  public BloodUnit(UUID facilityId, String governorate, String unitCode, String bloodGroup, String component, LocalDate donatedAt, LocalDate expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.unitCode=unitCode; this.bloodGroup=bloodGroup;
    this.component=component==null?"RBC":component; this.donatedAt=donatedAt; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public String getUnitCode(){return unitCode;} public String getBloodGroup(){return bloodGroup;}
  public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public LocalDate getExpiresAt(){return expiresAt;}
  public void reserveFor(UUID patientId){this.status="reserved"; this.reservedForPatient=patientId; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
