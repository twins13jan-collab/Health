package eg.gov.health.platform.majorincidents;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "reserve_rosters")
public class ReserveRosterEntry {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="member_ref") private String memberRef;
  @Column(name="member_type") private String memberType;
  private String role;
  @Column(nullable=false) private boolean verified = false;
  @Column(nullable=false) private boolean deployed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ReserveRosterEntry() {}
  public ReserveRosterEntry(UUID facilityId, String governorate, UUID planId, String memberRef, String memberType, String role, boolean verified) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.memberRef=memberRef; this.memberType=memberType; this.role=role; this.verified=verified;
  }
  public UUID getId(){return id;} public boolean isVerified(){return verified;}
  public void deploy(){ this.deployed=true; }
}
