package eg.gov.health.platform.sensory;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "assistive_devices")
public class AssistiveDevice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="device_type", nullable=false) private String deviceType;
  private String specification;
  @Column(name="fitting_status", nullable=false) private String fittingStatus = "pending";
  @Column(name="fitting_notes") private String fittingNotes;
  @Column(name="delivered_at") private OffsetDateTime deliveredAt;
  @Column(name="maintenance_due") private LocalDate maintenanceDue;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AssistiveDevice() {}
  public AssistiveDevice(UUID facilityId, String governorate, UUID patientId, String deviceType, String specification, LocalDate maintenanceDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.deviceType=deviceType;
    this.specification=specification; this.maintenanceDue=maintenanceDue;
  }
  public UUID getId(){return id;} public String getFittingStatus(){return fittingStatus;}
  public void fit(boolean fitted, String notes){ this.fittingStatus = fitted ? "fitted" : "refit_required"; this.fittingNotes=notes; this.updatedAt=OffsetDateTime.now(); }
  public void deliver(){ this.fittingStatus="delivered"; this.deliveredAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
