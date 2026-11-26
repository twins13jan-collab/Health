package eg.gov.health.platform.softwarelifecycle;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "software_components")
public class SoftwareComponent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String kind;
  @Column(name="third_party", nullable=false) private boolean thirdParty = false;
  @Column(name="licence_ref") private String licenceRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SoftwareComponent() {}
  public SoftwareComponent(UUID facilityId, String governorate, String name, String kind, boolean thirdParty, String licenceRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.kind=kind; this.thirdParty=thirdParty; this.licenceRef=licenceRef;
  }
  public UUID getId(){return id;} public boolean isThirdParty(){return thirdParty;} public String getLicenceRef(){return licenceRef;}
}
