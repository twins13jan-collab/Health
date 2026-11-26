package eg.gov.health.platform.homecare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "home_devices")
public class HomeDevice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="device_type", nullable=false) private String deviceType;
  @Column(name="serial_number", nullable=false) private String serialNumber;
  @Column(name="condition_on_delivery", nullable=false) private String conditionOnDelivery;
  @Column(name="maintenance_due") private LocalDate maintenanceDue;
  @Column(name="delivered_at") private OffsetDateTime deliveredAt = OffsetDateTime.now();
  @Column(name="retrieved_at") private OffsetDateTime retrievedAt;
  @Column(nullable=false) private String status = "delivered";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HomeDevice() {}
  public HomeDevice(UUID facilityId, String governorate, UUID patientId, String deviceType, String serialNumber, String conditionOnDelivery, LocalDate maintenanceDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.deviceType=deviceType;
    this.serialNumber=serialNumber; this.conditionOnDelivery=conditionOnDelivery; this.maintenanceDue=maintenanceDue;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public LocalDate getMaintenanceDue(){return maintenanceDue;}
  public void retrieve(){ this.status="retrieved"; this.retrievedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
