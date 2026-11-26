package eg.gov.health.platform.orgchange;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "change_champions")
public class ChangeChampion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="initiative_id", nullable=false) private UUID initiativeId;
  private String champion; private String department;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ChangeChampion() {}
  public ChangeChampion(UUID facilityId, String governorate, UUID initiativeId, String champion, String department) {
    this.facilityId=facilityId; this.governorate=governorate; this.initiativeId=initiativeId; this.champion=champion; this.department=department;
  }
  public UUID getId(){return id;}
}
