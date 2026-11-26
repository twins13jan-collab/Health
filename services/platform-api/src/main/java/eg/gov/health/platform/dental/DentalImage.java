package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dental_images")
public class DentalImage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="procedure_id") private UUID procedureId;
  @Column(name="image_type", nullable=false) private String imageType;
  private String device; private String region;
  @Column(name="taken_at") private OffsetDateTime takenAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DentalImage() {}
  public DentalImage(UUID facilityId, String governorate, UUID patientId, UUID procedureId, String imageType, String device, String region) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.procedureId=procedureId;
    this.imageType=imageType; this.device=device; this.region=region;
  }
  public UUID getId(){return id;}
}
