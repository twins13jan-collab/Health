package eg.gov.health.platform.emergency;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "ambulance_units")
public class AmbulanceUnit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="unit_code", nullable=false, unique=true) private String unitCode;
  @Column(nullable=false) private Double lat; @Column(nullable=false) private Double lon;
  @Column(nullable=false) private String status = "available"; // available, dispatched, on_scene, transporting, returning, offline
  @Column private String capabilities = ""; // comma list: ALS, BLS, NEONATAL, ICU
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AmbulanceUnit() {}
  public AmbulanceUnit(UUID facilityId, String governorate, String unitCode, Double lat, Double lon, String capabilities) {
    this.facilityId=facilityId; this.governorate=governorate; this.unitCode=unitCode; this.lat=lat; this.lon=lon; this.capabilities=capabilities==null?"":capabilities;
  }
  public UUID getId(){return id;} public String getUnitCode(){return unitCode;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public Double getLat(){return lat;} public Double getLon(){return lon;}
  public java.util.Set<String> capabilitySet(){ return capabilities==null||capabilities.isBlank()? java.util.Set.of() : java.util.Set.of(capabilities.split(",")); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public void moveTo(Double la, Double lo){this.lat=la; this.lon=lo; this.updatedAt=OffsetDateTime.now();}
}
