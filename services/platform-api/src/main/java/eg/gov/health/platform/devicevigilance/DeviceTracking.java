package eg.gov.health.platform.devicevigilance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "device_tracking")
public class DeviceTracking {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_ref") private String deviceRef;
  @Column(name="serial_no") private String serialNo;
  private String location;
  @Column(nullable=false) private String status = "in_service";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeviceTracking() {}
  public DeviceTracking(UUID facilityId, String governorate, String deviceRef, String serialNo, String location) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceRef=deviceRef; this.serialNo=serialNo; this.location=location;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
