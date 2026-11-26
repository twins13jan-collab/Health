package eg.gov.health.platform.rehab;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "therapy_sessions")
public class TherapySession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="session_type", nullable=false) private String sessionType;
  private String intervention;
  @Column(name="duration_min") private Integer durationMin;
  @Column(name="pain_during") private Integer painDuring;
  private String response;
  @Column(nullable=false) private String incident = "none";
  @Column(nullable=false) private String status = "completed";
  @Column(name="missed_reason") private String missedReason;
  private String therapist;
  @Column(name="session_at") private OffsetDateTime sessionAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TherapySession() {}
  public TherapySession(UUID facilityId, String governorate, UUID planId, String sessionType, String intervention,
                        Integer durationMin, Integer painDuring, String response, String incident, String status, String missedReason, String therapist) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.sessionType=sessionType;
    this.intervention=intervention; this.durationMin=durationMin; this.painDuring=painDuring; this.response=response;
    if (incident!=null) this.incident=incident; if (status!=null) this.status=status; this.missedReason=missedReason; this.therapist=therapist;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
