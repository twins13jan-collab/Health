package eg.gov.health.platform.ethicscommittees;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "committee_members")
public class CommitteeMember {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="committee_id", nullable=false) private UUID committeeId;
  @Column(name="member_ref") private String memberRef;
  @Column(name="coi_declared", nullable=false) private boolean coiDeclared = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CommitteeMember() {}
  public CommitteeMember(UUID facilityId, String governorate, UUID committeeId, String memberRef, boolean coiDeclared) {
    this.facilityId=facilityId; this.governorate=governorate; this.committeeId=committeeId; this.memberRef=memberRef; this.coiDeclared=coiDeclared;
  }
  public UUID getId(){return id;} public String getMemberRef(){return memberRef;} public boolean isCoiDeclared(){return coiDeclared;}
}
