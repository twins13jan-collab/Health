package eg.gov.health.platform.facilityestablishment;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "opening_licences")
public class OpeningLicence {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="project_id", nullable=false) private UUID projectId;
  @Column(name="licence_ref") private String licenceRef;
  @Column(nullable=false) private boolean granted = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OpeningLicence() {}
  public OpeningLicence(UUID facilityId, String governorate, UUID projectId, String licenceRef, boolean granted) {
    this.facilityId=facilityId; this.governorate=governorate; this.projectId=projectId; this.licenceRef=licenceRef; this.granted=granted;
  }
  public UUID getId(){return id;} public boolean isGranted(){return granted;}
}
