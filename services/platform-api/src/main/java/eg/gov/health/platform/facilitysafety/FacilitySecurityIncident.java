package eg.gov.health.platform.facilitysafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "facility_security_incidents")
public class FacilitySecurityIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String kind;
  private String location; private String severity;
  @Column(name="reported_by") private String reportedBy;
  @Column(name="action_taken") private String actionTaken;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FacilitySecurityIncident() {}
  public FacilitySecurityIncident(UUID facilityId, String governorate, String kind, String location, String severity, String reportedBy, String actionTaken) {
    this.facilityId=facilityId; this.governorate=governorate; this.kind=kind; this.location=location; this.severity=severity;
    this.reportedBy=reportedBy; this.actionTaken=actionTaken;
  }
  public UUID getId(){return id;}
}
