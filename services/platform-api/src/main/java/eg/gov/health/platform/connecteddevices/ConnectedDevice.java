package eg.gov.health.platform.connecteddevices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "connected_devices")
public class ConnectedDevice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_ref") private String deviceRef;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="calibration_due") private LocalDate calibrationDue;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ConnectedDevice() {}
  public ConnectedDevice(UUID facilityId, String governorate, String deviceRef, String patientRef, LocalDate calibrationDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceRef=deviceRef; this.patientRef=patientRef; this.calibrationDue=calibrationDue;
  }
  public UUID getId(){return id;} public LocalDate getCalibrationDue(){return calibrationDue;}
}
