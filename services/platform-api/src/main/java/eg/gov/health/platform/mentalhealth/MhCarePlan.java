package eg.gov.health.platform.mentalhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mh_care_plans")
public class MhCarePlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="episode_id", nullable=false) private UUID episodeId;
  private String goals; private String interventions;
  @Column(name="medications_summary") private String medicationsSummary;
  @Column(name="review_date") private LocalDate reviewDate;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MhCarePlan() {}
  public MhCarePlan(UUID facilityId, String governorate, UUID episodeId, String goals, String interventions, String meds, LocalDate reviewDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.episodeId=episodeId; this.goals=goals;
    this.interventions=interventions; this.medicationsSummary=meds; this.reviewDate=reviewDate;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
