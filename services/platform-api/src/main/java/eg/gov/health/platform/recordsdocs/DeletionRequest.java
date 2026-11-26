package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "deletion_requests")
public class DeletionRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="document_id", nullable=false) private UUID documentId;
  @Column(name="requested_by") private String requestedBy;
  private String basis; @Column(name="blocked_reason") private String blockedReason;
  @Column(nullable=false) private String status = "held";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeletionRequest() {}
  public DeletionRequest(UUID facilityId, String governorate, UUID documentId, String requestedBy, String basis, String blockedReason, String status) {
    this.facilityId=facilityId; this.governorate=governorate; this.documentId=documentId; this.requestedBy=requestedBy;
    this.basis=basis; this.blockedReason=blockedReason; this.status=status;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
