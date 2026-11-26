package eg.gov.health.platform.continuity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "restore_tests")
public class RestoreTest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_id", nullable=false) private UUID serviceId;
  @Column(name="tested_at") private LocalDate testedAt;
  @Column(name="actual_rto_minutes") private Integer actualRtoMinutes;
  @Column(name="actual_rpo_minutes") private Integer actualRpoMinutes;
  @Column(nullable=false) private boolean success = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RestoreTest() {}
  public RestoreTest(UUID facilityId, String governorate, UUID serviceId, LocalDate testedAt, Integer actualRtoMinutes, Integer actualRpoMinutes, boolean success) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceId=serviceId; this.testedAt=testedAt; this.actualRtoMinutes=actualRtoMinutes;
    this.actualRpoMinutes=actualRpoMinutes; this.success=success;
  }
  public UUID getId(){return id;} public LocalDate getTestedAt(){return testedAt;}
  public Integer getActualRtoMinutes(){return actualRtoMinutes;} public Integer getActualRpoMinutes(){return actualRpoMinutes;} public boolean isSuccess(){return success;}
}
