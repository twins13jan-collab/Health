package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "research_requests")
public class ResearchRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="requester", nullable=false) private String requester;
  @Column(nullable=false) private String purpose;
  @Column(name="requested_fields", nullable=false) private String requestedFields;
  @Column(nullable=false) private String status = "submitted"; // submitted, under_review, approved, rejected, extracted
  @Column(name="ethics_decision") private String ethicsDecision;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ResearchRequest() {}
  public ResearchRequest(UUID facilityId, String governorate, String requester, String purpose, String requestedFields) {
    this.facilityId=facilityId; this.governorate=governorate; this.requester=requester; this.purpose=purpose; this.requestedFields=requestedFields;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public java.util.List<String> fieldList(){ return requestedFields==null||requestedFields.isBlank()? java.util.List.of() : java.util.List.of(requestedFields.split(",")); }
  public void decide(String decision, String status){ this.ethicsDecision=decision; this.status=status; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
