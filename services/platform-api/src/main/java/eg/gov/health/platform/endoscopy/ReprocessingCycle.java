package eg.gov.health.platform.endoscopy;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "reprocessing_cycles")
public class ReprocessingCycle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="scope_id", nullable=false) private UUID scopeId;
  @Column(name="cleaning_done", nullable=false) private boolean cleaningDone = false;
  @Column(name="disinfection_done", nullable=false) private boolean disinfectionDone = false;
  @Column(name="leak_test_passed", nullable=false) private boolean leakTestPassed = false;
  private String operator;
  @Column(nullable=false) private String status;
  @Column(name="completed_at") private OffsetDateTime completedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ReprocessingCycle() {}
  public ReprocessingCycle(UUID facilityId, String governorate, UUID scopeId, boolean cleaning, boolean disinfection, boolean leakTest, String operator) {
    this.facilityId=facilityId; this.governorate=governorate; this.scopeId=scopeId; this.cleaningDone=cleaning;
    this.disinfectionDone=disinfection; this.leakTestPassed=leakTest; this.operator=operator;
    this.status=EndoscopyRules.reprocessingPassed(cleaning, disinfection, leakTest) ? "passed" : "failed";
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
