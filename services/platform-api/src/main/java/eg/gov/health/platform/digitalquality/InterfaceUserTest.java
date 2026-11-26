package eg.gov.health.platform.digitalquality;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "interface_user_tests")
public class InterfaceUserTest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String surface; private Integer participants;
  @Column(name="task_success_pct") private BigDecimal taskSuccessPct;
  private String findings;
  @Column(name="tested_at") private OffsetDateTime testedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected InterfaceUserTest() {}
  public InterfaceUserTest(UUID facilityId, String governorate, String surface, Integer participants, BigDecimal taskSuccessPct, String findings) {
    this.facilityId=facilityId; this.governorate=governorate; this.surface=surface; this.participants=participants;
    this.taskSuccessPct=taskSuccessPct; this.findings=findings; this.testedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
