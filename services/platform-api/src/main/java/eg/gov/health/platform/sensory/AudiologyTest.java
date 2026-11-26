package eg.gov.health.platform.sensory;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "audiology_tests")
public class AudiologyTest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String ear;
  @Column(name="test_type", nullable=false) private String testType;
  @Column(name="device_code") private String deviceCode;
  @Column(name="calibration_valid", nullable=false) private boolean calibrationValid = false;
  @Column(name="calibration_date") private LocalDate calibrationDate;
  @Column(name="result_summary") private String resultSummary;
  @Column(nullable=false) private String status;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AudiologyTest() {}
  public AudiologyTest(UUID facilityId, String governorate, UUID patientId, String ear, String testType, String deviceCode,
                       boolean calibrationValid, LocalDate calibrationDate, String resultSummary) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.ear=ear; this.testType=testType;
    this.deviceCode=deviceCode; this.calibrationValid=calibrationValid; this.calibrationDate=calibrationDate;
    this.resultSummary=resultSummary;
    this.status = SensoryRules.calibrationCurrent(calibrationValid, calibrationDate, LocalDate.now()) ? "valid" : "invalid_calibration";
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
