package eg.gov.health.platform.citizenportal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "portal_disclosures")
public class PortalDisclosure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_id", nullable=false) private UUID accountId;
  @Column(name="resource_type") private String resourceType;
  @Column(name="resource_ref") private String resourceRef;
  @Column(name="accessed_by") private String accessedBy;
  private String channel;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PortalDisclosure() {}
  public PortalDisclosure(UUID facilityId, String governorate, UUID accountId, String resourceType, String resourceRef, String accessedBy, String channel) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountId=accountId; this.resourceType=resourceType;
    this.resourceRef=resourceRef; this.accessedBy=accessedBy; this.channel=channel;
  }
  public UUID getId(){return id;}
}
