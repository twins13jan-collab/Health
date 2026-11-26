package eg.gov.health.platform.cssd;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "instrument_sets")
public class InstrumentSet {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="set_code", nullable=false) private String setCode;
  private String name;
  @Column(name="contains_implants", nullable=false) private boolean containsImplants = false;
  @Column(name="storage_location") private String storageLocation;
  @Column(name="expiry_date") private LocalDate expiryDate;
  @Column(nullable=false) private String status = "in_use";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InstrumentSet() {}
  public InstrumentSet(UUID facilityId, String governorate, String setCode, String name, boolean containsImplants) {
    this.facilityId=facilityId; this.governorate=governorate; this.setCode=setCode; this.name=name; this.containsImplants=containsImplants;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public boolean isContainsImplants(){return containsImplants;} public LocalDate getExpiryDate(){return expiryDate;}
  public String getSetCode(){return setCode;} public String getName(){return name;}
  public String getStorageLocation(){return storageLocation;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void store(String location, LocalDate expiry){ this.storageLocation=location; this.expiryDate=expiry; this.status="sterile"; this.updatedAt=OffsetDateTime.now(); }
}
