package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "document_records")
public class DocumentRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="doc_type", nullable=false) private String docType;
  private String title; private String owner; private String classification;
  private String confidentiality; private String source;
  @Column(name="current_version", nullable=false) private int currentVersion = 1;
  @Column(name="approved_version") private Integer approvedVersion;
  @Column(nullable=false) private boolean corrupted = false;
  @Column(name="restoration_ref") private String restorationRef;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DocumentRecord() {}
  public DocumentRecord(UUID facilityId, String governorate, String docType, String title, String owner, String classification, String confidentiality, String source) {
    this.facilityId=facilityId; this.governorate=governorate; this.docType=docType; this.title=title; this.owner=owner;
    this.classification=classification; this.confidentiality=confidentiality; this.source=source;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public int getCurrentVersion(){return currentVersion;}
  public String getDocType(){return docType;} public String getStatus(){return status;} public Integer getApprovedVersion(){return approvedVersion;}
  public OffsetDateTime getCreatedAt(){return createdAt;}
  public void bumpVersion(){ this.currentVersion++; this.updatedAt=OffsetDateTime.now(); }
  public void approve(int version){ this.approvedVersion=version; this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
  public void markCorrupted(String restorationRef){ this.corrupted=true; this.restorationRef=restorationRef; this.updatedAt=OffsetDateTime.now(); }
}
