package eg.gov.health.platform.proms;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "prom_responses")
public class PromResponse {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="schedule_id", nullable=false) private UUID scheduleId;
  private BigDecimal score;
  @Column(name="urgent_signal", nullable=false) private boolean urgentSignal = false;
  @Column(name="urgent_followed_up", nullable=false) private boolean urgentFollowedUp = false;
  @Column(name="responded_at") private OffsetDateTime respondedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PromResponse() {}
  public PromResponse(UUID facilityId, String governorate, UUID scheduleId, BigDecimal score, boolean urgentSignal) {
    this.facilityId=facilityId; this.governorate=governorate; this.scheduleId=scheduleId; this.score=score; this.urgentSignal=urgentSignal;
    this.respondedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public boolean isUrgentSignal(){return urgentSignal;} public boolean isUrgentFollowedUp(){return urgentFollowedUp;}
  public void followUpUrgent(){ this.urgentFollowedUp=true; this.updatedAt=OffsetDateTime.now(); }
}
