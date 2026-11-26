package eg.gov.health.platform.medicaltourism;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "intl_followup_contacts")
public class IntlFollowupContact {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id", nullable=false) private UUID requestId;
  @Column(name="home_provider") private String homeProvider;
  @Column(name="patient_permission", nullable=false) private boolean patientPermission = false;
  @Column(nullable=false) private boolean contacted = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IntlFollowupContact() {}
  public IntlFollowupContact(UUID facilityId, String governorate, UUID requestId, String homeProvider, boolean patientPermission, boolean contacted) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.homeProvider=homeProvider;
    this.patientPermission=patientPermission; this.contacted=contacted;
  }
  public UUID getId(){return id;}
}
