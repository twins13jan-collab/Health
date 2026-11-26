package eg.gov.health.platform.medicalassets;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "device_incidents")
public class DeviceIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  private String reporter; private String description;
  @Column(name="patient_affected", nullable=false) private boolean patientAffected = false;
  @Column(name="safety_case_ref") private String safetyCaseRef;
  private String investigation; @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeviceIncident() {}
  public DeviceIncident(UUID facilityId, String governorate, UUID assetId, String reporter, String description, boolean patientAffected, String safetyCaseRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetId=assetId; this.reporter=reporter;
    this.description=description; this.patientAffected=patientAffected; this.safetyCaseRef=safetyCaseRef;
  }
  public UUID getId(){return id;}
  public UUID getAssetId(){return assetId;} public String getDescription(){return description;}
  public boolean isPatientAffected(){return patientAffected;} public String getSafetyCaseRef(){return safetyCaseRef;}
  public String getStatus(){return status;} public String getReporter(){return reporter;}
  public OffsetDateTime getCreatedAt(){return createdAt;}
}
