package eg.gov.health.platform.citizenportal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "citizen_requests")
public class CitizenRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_id") private UUID accountId;
  private String kind; private String subject; private String body;
  @Column(name="assigned_to") private String assignedTo;
  private String response; @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CitizenRequest() {}
  public CitizenRequest(UUID facilityId, String governorate, UUID accountId, String kind, String subject, String body) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountId=accountId; this.kind=kind; this.subject=subject; this.body=body;
  }
  public UUID getId(){return id;}
  public void respond(String by, String response){ this.assignedTo=by; this.response=response; this.status="answered"; this.updatedAt=OffsetDateTime.now(); }
}
