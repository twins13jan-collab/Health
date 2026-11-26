package eg.gov.health.platform.stroke;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "stroke_discharge_plans")
public class StrokeDischargePlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="activation_id", nullable=false) private UUID activationId;
  @Column(name="rehab_referral") private String rehabReferral;
  @Column(name="secondary_prevention") private String secondaryPrevention;
  @Column(name="follow_up") private String followUp;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected StrokeDischargePlan() {}
  public StrokeDischargePlan(UUID facilityId, String governorate, UUID activationId, String rehabReferral, String secondaryPrevention, String followUp) {
    this.facilityId=facilityId; this.governorate=governorate; this.activationId=activationId; this.rehabReferral=rehabReferral;
    this.secondaryPrevention=secondaryPrevention; this.followUp=followUp;
  }
  public UUID getId(){return id;}
}
