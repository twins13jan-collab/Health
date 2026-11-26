package eg.gov.health.platform.construction;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "commissioning_tests")
public class CommissioningTest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="project_id", nullable=false) private UUID projectId;
  @Column(name="system_name") private String systemName;
  private String result;
  @Column(name="tested_by") private String testedBy;
  @Column(name="tested_at") private OffsetDateTime testedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CommissioningTest() {}
  public CommissioningTest(UUID facilityId, String governorate, UUID projectId, String systemName, String result, String testedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.projectId=projectId; this.systemName=systemName; this.result=result;
    this.testedBy=testedBy; this.testedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public String getSystemName(){return systemName;} public String getResult(){return result;}
}
