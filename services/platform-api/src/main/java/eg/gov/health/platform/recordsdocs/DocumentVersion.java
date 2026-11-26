package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "document_versions")
public class DocumentVersion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="document_id", nullable=false) private UUID documentId;
  @Column(nullable=false) private int version;
  @Column(name="content_ref") private String contentRef;
  @Column(name="content_hash", nullable=false) private String contentHash;
  private String author; @Column(name="change_reason") private String changeReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DocumentVersion() {}
  public DocumentVersion(UUID facilityId, String governorate, UUID documentId, int version, String contentRef, String contentHash, String author, String changeReason) {
    this.facilityId=facilityId; this.governorate=governorate; this.documentId=documentId; this.version=version;
    this.contentRef=contentRef; this.contentHash=contentHash; this.author=author; this.changeReason=changeReason;
  }
  public UUID getId(){return id;} public String getContentHash(){return contentHash;} public int getVersion(){return version;}
}
