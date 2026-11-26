package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "implant_records")
public class ImplantRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String manufacturer;
  @Column(name="lot_number", nullable=false) private String lotNumber;
  @Column(name="serial_number") private String serialNumber;
  @Column(name="tooth_number") private Integer toothNumber;
  @Column(nullable=false) private String status = "implanted";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ImplantRecord() {}
  public ImplantRecord(UUID facilityId, String governorate, UUID patientId, String manufacturer, String lotNumber, String serialNumber, Integer toothNumber) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.manufacturer=manufacturer;
    this.lotNumber=lotNumber; this.serialNumber=serialNumber; this.toothNumber=toothNumber;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void recall(){ this.status="recalled"; this.updatedAt=OffsetDateTime.now(); }
}
