package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dental_charts")
public class DentalChart {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private int version = 1;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DentalChart() {}
  public DentalChart(UUID facilityId, String governorate, UUID patientId, int version) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.version=version;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public int getVersion(){return version;} public String getStatus(){return status;}
  public void supersede(){ this.status="superseded"; this.updatedAt=OffsetDateTime.now(); }
}
