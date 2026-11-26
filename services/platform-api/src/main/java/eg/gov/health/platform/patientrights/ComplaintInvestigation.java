package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "complaint_investigations")
public class ComplaintInvestigation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complaint_id", nullable=false) private UUID complaintId;
  @Column(nullable=false) private String investigator;
  @Column(name="conflict_declared", nullable=false) private boolean conflictDeclared = false;
  @Column(name="reassigned_from") private String reassignedFrom;
  private String timeline; @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ComplaintInvestigation() {}
  public ComplaintInvestigation(UUID facilityId, String governorate, UUID complaintId, String investigator, String reassignedFrom) {
    this.facilityId=facilityId; this.governorate=governorate; this.complaintId=complaintId; this.investigator=investigator; this.reassignedFrom=reassignedFrom;
  }
  public UUID getId(){return id;} public String getInvestigator(){return investigator;} public String getStatus(){return status;}
  public void declareConflict(){ this.conflictDeclared=true; this.status="reassigned"; this.updatedAt=OffsetDateTime.now(); }
  public void complete(String timeline){ this.timeline=timeline; this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
