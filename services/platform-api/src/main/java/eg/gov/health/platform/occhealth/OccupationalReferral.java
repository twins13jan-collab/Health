package eg.gov.health.platform.occhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "occupational_referrals")
public class OccupationalReferral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_id", nullable=false) private UUID workerId;
  private String reason;
  @Column(name="report_shared", nullable=false) private boolean reportShared = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OccupationalReferral() {}
  public OccupationalReferral(UUID facilityId, String governorate, UUID workerId, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerId=workerId; this.reason=reason;
  }
  public UUID getId(){return id;}
}
