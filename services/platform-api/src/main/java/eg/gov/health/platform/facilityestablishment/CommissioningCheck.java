package eg.gov.health.platform.facilityestablishment;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "commissioning_checks")
public class CommissioningCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="project_id", nullable=false) private UUID projectId;
  @Column(name="check_type") private String checkType;
  @Column(nullable=false) private boolean passed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CommissioningCheck() {}
  public CommissioningCheck(UUID facilityId, String governorate, UUID projectId, String checkType, boolean passed) {
    this.facilityId=facilityId; this.governorate=governorate; this.projectId=projectId; this.checkType=checkType; this.passed=passed;
  }
  public UUID getId(){return id;} public String getCheckType(){return checkType;} public boolean isPassed(){return passed;}
}
