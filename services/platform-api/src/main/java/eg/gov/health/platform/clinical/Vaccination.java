package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "vaccinations")
public class Vaccination {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String vaccine;
  @Column(name="dose_number") private Integer doseNumber;
  @Column(name="given_at", nullable=false) private LocalDate givenAt;
  @Column(name="batch_number") private String batchNumber;
  @Column(nullable=false) private String status = "given"; // given, refused, contraindicated
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Vaccination() {}
  public Vaccination(UUID facilityId, String governorate, UUID patientId, String vaccine, Integer doseNumber, LocalDate givenAt, String batchNumber) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.vaccine=vaccine;
    this.doseNumber=doseNumber; this.givenAt=givenAt; this.batchNumber=batchNumber;
  }
  public UUID getId(){return id;} public String getVaccine(){return vaccine;} public UUID getPatientId(){return patientId;}
}
