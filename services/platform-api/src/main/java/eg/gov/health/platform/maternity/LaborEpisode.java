package eg.gov.health.platform.maternity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "labor_episodes")
public class LaborEpisode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="pregnancy_id", nullable=false) private UUID pregnancyId;
  @Column(name="started_at") private OffsetDateTime startedAt = OffsetDateTime.now();
  @Column(name="cervix_dilation_cm") private Double cervixDilationCm;
  private String team;
  @Column(nullable=false) private String status = "in_labor";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LaborEpisode() {}
  public LaborEpisode(UUID facilityId, String governorate, UUID pregnancyId, Double cervix, String team) {
    this.facilityId=facilityId; this.governorate=governorate; this.pregnancyId=pregnancyId; this.cervixDilationCm=cervix; this.team=team;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public UUID getPregnancyId(){return pregnancyId;} public OffsetDateTime getStartedAt(){return startedAt;}
  public Double getCervixDilationCm(){return cervixDilationCm;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
