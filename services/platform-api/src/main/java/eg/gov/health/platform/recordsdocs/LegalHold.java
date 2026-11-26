package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "legal_holds")
public class LegalHold {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="document_id", nullable=false) private UUID documentId;
  @Column(name="case_ref") private String caseRef;
  private String reason; @Column(name="placed_by") private String placedBy;
  @Column(name="released_by") private String releasedBy;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LegalHold() {}
  public LegalHold(UUID facilityId, String governorate, UUID documentId, String caseRef, String reason, String placedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.documentId=documentId; this.caseRef=caseRef; this.reason=reason; this.placedBy=placedBy;
  }
  public UUID getId(){return id;} public boolean isActive(){return active;}
  public void release(String by){ this.active=false; this.releasedBy=by; this.updatedAt=OffsetDateTime.now(); }
}
