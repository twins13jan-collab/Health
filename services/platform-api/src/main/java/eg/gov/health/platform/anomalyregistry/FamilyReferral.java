package eg.gov.health.platform.anomalyregistry;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "family_referrals")
public class FamilyReferral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  private String service; private Boolean accepted;
  @Column(name="referred_by") private String referredBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected FamilyReferral() {}
  public FamilyReferral(UUID facilityId, String governorate, UUID caseId, String service, String referredBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.service=service; this.referredBy=referredBy;
  }
  public UUID getId(){return id;}
}
