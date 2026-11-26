package eg.gov.health.platform.infectioncontrol;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "contact_tracings")
public class ContactTracing {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="contact_ref") private String contactRef;
  @Column(name="exposure_type") private String exposureType;
  @Column(name="risk_level") private String riskLevel;
  @Column(name="follow_up_status", nullable=false) private String followUpStatus = "pending";
  @Column(name="test_result") private String testResult;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ContactTracing() {}
  public ContactTracing(UUID facilityId, String governorate, UUID caseId, String contactRef, String exposureType, String riskLevel) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.contactRef=contactRef; this.exposureType=exposureType; this.riskLevel=riskLevel;
  }
  public UUID getId(){return id;}
  public void followUp(String status, String result){ this.followUpStatus=status; this.testResult=result; this.updatedAt=OffsetDateTime.now(); }
}
