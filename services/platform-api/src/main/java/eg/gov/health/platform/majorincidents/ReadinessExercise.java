package eg.gov.health.platform.majorincidents;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "readiness_exercises")
public class ReadinessExercise {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="exercise_type") private String exerciseType;
  @Column(name="conducted_at") private LocalDate conductedAt;
  @Column(name="lessons_recorded", nullable=false) private boolean lessonsRecorded = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ReadinessExercise() {}
  public ReadinessExercise(UUID facilityId, String governorate, UUID planId, String exerciseType, LocalDate conductedAt, boolean lessonsRecorded) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.exerciseType=exerciseType; this.conductedAt=conductedAt; this.lessonsRecorded=lessonsRecorded;
  }
  public UUID getId(){return id;} public LocalDate getConductedAt(){return conductedAt;}
}
