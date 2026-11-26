package eg.gov.health.platform.security;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "lessons_learned")
public class LessonsLearned {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(nullable=false) private String lessons;
  @Column(name="corrective_actions") private String correctiveActions;
  @Column(name="corrective_closed", nullable=false) private boolean correctiveClosed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LessonsLearned() {}
  public LessonsLearned(UUID facilityId, String governorate, UUID incidentId, String lessons, String correctiveActions) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.lessons=lessons; this.correctiveActions=correctiveActions;
  }
  public UUID getId(){return id;} public boolean isCorrectiveClosed(){return correctiveClosed;}
  public void closeCorrective(){ this.correctiveClosed=true; this.updatedAt=OffsetDateTime.now(); }
}
