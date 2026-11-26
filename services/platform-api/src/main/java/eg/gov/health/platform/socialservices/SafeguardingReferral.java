package eg.gov.health.platform.socialservices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "safeguarding_referrals")
public class SafeguardingReferral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  @Column(name="concern_type") private String concernType;
  @Column(name="referred_to") private String referredTo;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SafeguardingReferral() {}
  public SafeguardingReferral(UUID facilityId, String governorate, UUID assessmentId, String concernType, String referredTo) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.concernType=concernType; this.referredTo=referredTo;
  }
  public UUID getId(){return id;}
}
