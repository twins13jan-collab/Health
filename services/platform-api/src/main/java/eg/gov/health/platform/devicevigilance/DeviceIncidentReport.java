package eg.gov.health.platform.devicevigilance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "device_incident_reports")
public class DeviceIncidentReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="device_ref") private String deviceRef;
  @Column(name="serial_no") private String serialNo;
  @Column(name="lot_no") private String lotNo;
  @Column(name="incident_type") private String incidentType;
  private String severity; private String description;
  @Column(name="reported_by") private String reportedBy;
  @Column(name="linked_maintenance_ref") private String linkedMaintenanceRef;
  @Column(name="training_need", nullable=false) private boolean trainingNeed = false;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeviceIncidentReport() {}
  public DeviceIncidentReport(UUID facilityId, String governorate, String deviceRef, String serialNo, String lotNo, String incidentType,
                              String severity, String description, String reportedBy, String linkedMaintenanceRef, boolean trainingNeed) {
    this.facilityId=facilityId; this.governorate=governorate; this.deviceRef=deviceRef; this.serialNo=serialNo; this.lotNo=lotNo;
    this.incidentType=incidentType; this.severity=severity; this.description=description; this.reportedBy=reportedBy;
    this.linkedMaintenanceRef=linkedMaintenanceRef; this.trainingNeed=trainingNeed;
  }
  public UUID getId(){return id;} public String getSeverity(){return severity;} public String getIncidentType(){return incidentType;}
  public String getDeviceRef(){return deviceRef;} public String getSerialNo(){return serialNo;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
