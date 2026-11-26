package eg.gov.health.platform.unknownpatient;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "temporary_identities")
public class TemporaryIdentity {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="temp_code", nullable=false) private String tempCode;
  private String description; private String circumstance;
  @Column(name="protection_flag", nullable=false) private boolean protectionFlag = false;
  @Column(name="created_by") private String createdBy;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TemporaryIdentity() {}
  public TemporaryIdentity(UUID facilityId, String governorate, String tempCode, String description, String circumstance, boolean protectionFlag, String createdBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.tempCode=tempCode; this.description=description;
    this.circumstance=circumstance; this.protectionFlag=protectionFlag; this.createdBy=createdBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public String getCreatedBy(){return createdBy;} public boolean isProtectionFlag(){return protectionFlag;} public String getTempCode(){return tempCode;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
