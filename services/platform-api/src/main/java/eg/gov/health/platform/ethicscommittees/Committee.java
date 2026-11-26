package eg.gov.health.platform.ethicscommittees;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "committees")
public class Committee {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String mandate;
  @Column(name="quorum_required") private Integer quorumRequired;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected Committee() {}
  public Committee(UUID facilityId, String governorate, String name, String mandate, Integer quorumRequired) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.mandate=mandate; this.quorumRequired=quorumRequired;
  }
  public UUID getId(){return id;} public Integer getQuorumRequired(){return quorumRequired;}
}
