package eg.gov.health.platform.medicalassets;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "medical_assets")
public class MedicalAsset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_code", nullable=false) private String assetCode;
  private String type; private String manufacturer; private String model;
  @Column(name="serial_number") private String serialNumber;
  private String location; private String owner;
  @Column(name="calibration_due") private LocalDate calibrationDue;
  @Column(nullable=false) private String status = "in_service";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MedicalAsset() {}
  public MedicalAsset(UUID facilityId, String governorate, String assetCode, String type, String manufacturer, String model, String serialNumber, String location, String owner, LocalDate calibrationDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetCode=assetCode; this.type=type; this.manufacturer=manufacturer;
    this.model=model; this.serialNumber=serialNumber; this.location=location; this.owner=owner; this.calibrationDue=calibrationDue;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public LocalDate getCalibrationDue(){return calibrationDue;} public String getLocation(){return location;}
  public String getAssetCode(){return assetCode;} public String getType(){return type;}
  public String getManufacturer(){return manufacturer;} public String getModel(){return model;}
  public String getSerialNumber(){return serialNumber;} public String getOwner(){return owner;}
  public String getGovernorate(){return governorate;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void transfer(String loc){ this.location=loc; this.updatedAt=OffsetDateTime.now(); }
  public void setCalibrationDue(LocalDate d){ this.calibrationDue=d; this.updatedAt=OffsetDateTime.now(); }
}
