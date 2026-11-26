package eg.gov.health.platform.uxgov;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "usability_test_sessions")
public class UsabilityTestSession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="scenario_id", nullable=false) private UUID scenarioId;
  private String tester;
  @Column(name="participant_role") private String participantRole;
  @Column(name="task_completed", nullable=false) private boolean taskCompleted = false;
  @Column(name="completion_seconds") private Integer completionSeconds;
  @Column(name="errors_count") private Integer errorsCount;
  private String notes;
  @Column(name="satisfaction_score") private BigDecimal satisfactionScore;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected UsabilityTestSession() {}
  public UsabilityTestSession(UUID facilityId, String governorate, UUID scenarioId, String tester, String participantRole,
                              boolean taskCompleted, Integer completionSeconds, Integer errorsCount, String notes, BigDecimal satisfactionScore) {
    this.facilityId=facilityId; this.governorate=governorate; this.scenarioId=scenarioId; this.tester=tester; this.participantRole=participantRole;
    this.taskCompleted=taskCompleted; this.completionSeconds=completionSeconds; this.errorsCount=errorsCount; this.notes=notes; this.satisfactionScore=satisfactionScore;
  }
  public UUID getId(){return id;} public boolean isTaskCompleted(){return taskCompleted;}
}
