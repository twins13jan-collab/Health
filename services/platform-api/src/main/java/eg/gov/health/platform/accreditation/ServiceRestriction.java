package eg.gov.health.platform.accreditation;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "service_restrictions")
public class ServiceRestriction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="licence_id", nullable=false) private UUID licenceId;
  @Column(name="restriction_type") private String restrictionType;
  @Column(name="decided_by") private String decidedBy;
  private String authority; private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ServiceRestriction() {}
  public ServiceRestriction(UUID facilityId, String governorate, UUID licenceId, String restrictionType, String decidedBy, String authority, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.licenceId=licenceId; this.restrictionType=restrictionType;
    this.decidedBy=decidedBy; this.authority=authority; this.reason=reason;
  }
  public UUID getId(){return id;}
}
