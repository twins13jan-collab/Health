package eg.gov.health.platform.orglearning;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "org_lessons_learned")
public class LessonLearned {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="source_type") private String sourceType;
  @Column(name="source_ref") private String sourceRef;
  private String lesson;
  @Column(nullable=false) private boolean applied = false;
  @Column(name="applied_in") private String appliedIn;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LessonLearned() {}
  public LessonLearned(UUID facilityId, String governorate, String sourceType, String sourceRef, String lesson, boolean applied, String appliedIn) {
    this.facilityId=facilityId; this.governorate=governorate; this.sourceType=sourceType; this.sourceRef=sourceRef; this.lesson=lesson;
    this.applied=applied; this.appliedIn=appliedIn;
  }
  public UUID getId(){return id;} public boolean isApplied(){return applied;} public String getAppliedIn(){return appliedIn;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
