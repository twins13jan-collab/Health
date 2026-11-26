package eg.gov.health.platform.cssd;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sterilizer_records")
public class SterilizerRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="sterilizer_code", nullable=false) private String sterilizerCode;
  @Column(name="maintenance_due") private LocalDate maintenanceDue;
  @Column(name="calibration_due") private LocalDate calibrationDue;
  @Column(nullable=false) private String status = "operational";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SterilizerRecord() {}
  public SterilizerRecord(UUID facilityId, String governorate, String sterilizerCode, LocalDate maintenanceDue, LocalDate calibrationDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.sterilizerCode=sterilizerCode; this.maintenanceDue=maintenanceDue; this.calibrationDue=calibrationDue;
  }
  public UUID getId(){return id;} public String getSterilizerCode(){return sterilizerCode;}
  public LocalDate getMaintenanceDue(){return maintenanceDue;} public LocalDate getCalibrationDue(){return calibrationDue;}
  public String getStatus(){return status;}
}
