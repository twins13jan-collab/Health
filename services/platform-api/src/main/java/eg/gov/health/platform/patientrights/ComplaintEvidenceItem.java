package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "complaint_evidence_items")
public class ComplaintEvidenceItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complaint_id", nullable=false) private UUID complaintId;
  private String description; @Column(name="content_hash") private String contentHash;
  @Column(name="added_by") private String addedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ComplaintEvidenceItem() {}
  public ComplaintEvidenceItem(UUID facilityId, String governorate, UUID complaintId, String description, String contentHash, String addedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.complaintId=complaintId; this.description=description; this.contentHash=contentHash; this.addedBy=addedBy;
  }
  public UUID getId(){return id;} public UUID getComplaintId(){return complaintId;}
}
