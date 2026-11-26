package eg.gov.health.platform.diagnostics;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "lab_orders")
public class LabOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="test_code", nullable=false) private String testCode;
  @Column(name="test_name") private String testName;
  @Column(nullable=false) private String priority = "routine"; // routine, urgent, stat
  @Column(nullable=false) private String status = "ordered"; // ordered, specimen_collected, received, in_analysis, resulted, verified, corrected, cancelled
  @Column(name="ordered_by") private String orderedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LabOrder() {}
  public LabOrder(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String testCode, String testName, String priority, String orderedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.testCode=testCode; this.testName=testName; this.priority=priority==null?"routine":priority; this.orderedBy=orderedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public UUID getPatientId(){return patientId;} public String getTestCode(){return testCode;} public String getStatus(){return status;}
  public String getPriority(){return priority;} public UUID getEncounterId(){return encounterId;} public String getTestName(){return testName;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
