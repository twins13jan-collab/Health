package eg.gov.health.platform.capabilityaccess;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "service_capabilities")
public class ServiceCapability {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_ref") private String serviceRef;
  @Column(name="staff_ready", nullable=false) private boolean staffReady = false;
  @Column(name="equipment_ready", nullable=false) private boolean equipmentReady = false;
  @Column(name="licence_valid", nullable=false) private boolean licenceValid = false;
  @Column(name="protocol_linked", nullable=false) private boolean protocolLinked = false;
  @Column(name="last_verified_at") private OffsetDateTime lastVerifiedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ServiceCapability() {}
  public ServiceCapability(UUID facilityId, String governorate, String serviceRef, boolean staffReady, boolean equipmentReady,
                             boolean licenceValid, boolean protocolLinked, OffsetDateTime lastVerifiedAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceRef=serviceRef; this.staffReady=staffReady;
    this.equipmentReady=equipmentReady; this.licenceValid=licenceValid; this.protocolLinked=protocolLinked; this.lastVerifiedAt=lastVerifiedAt;
  }
  public UUID getId(){return id;} public boolean isStaffReady(){return staffReady;} public boolean isEquipmentReady(){return equipmentReady;}
  public boolean isLicenceValid(){return licenceValid;} public boolean isProtocolLinked(){return protocolLinked;}
  public OffsetDateTime getLastVerifiedAt(){return lastVerifiedAt;}
}
