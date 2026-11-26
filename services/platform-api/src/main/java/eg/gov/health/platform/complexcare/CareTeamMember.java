package eg.gov.health.platform.complexcare;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "care_team_members")
public class CareTeamMember {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="member_name") private String memberName;
  @Column(name="member_role") private String memberRole;
  private String responsibility;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CareTeamMember() {}
  public CareTeamMember(UUID facilityId, String governorate, UUID planId, String memberName, String memberRole, String responsibility) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.memberName=memberName; this.memberRole=memberRole; this.responsibility=responsibility;
  }
  public UUID getId(){return id;}
}
