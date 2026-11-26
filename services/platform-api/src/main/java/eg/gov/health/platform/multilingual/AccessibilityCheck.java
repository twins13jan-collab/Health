package eg.gov.health.platform.multilingual;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "accessibility_checks")
public class AccessibilityCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String surface;
  @Column(name="check_type") private String checkType;
  @Column(nullable=false) private boolean passed = false;
  @Column(name="tested_with_diverse_users", nullable=false) private boolean testedWithDiverseUsers = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AccessibilityCheck() {}
  public AccessibilityCheck(UUID facilityId, String governorate, String surface, String checkType, boolean passed, boolean testedWithDiverseUsers) {
    this.facilityId=facilityId; this.governorate=governorate; this.surface=surface; this.checkType=checkType; this.passed=passed;
    this.testedWithDiverseUsers=testedWithDiverseUsers;
  }
  public UUID getId(){return id;}
}
