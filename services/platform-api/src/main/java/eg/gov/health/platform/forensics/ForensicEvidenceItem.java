package eg.gov.health.platform.forensics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "forensic_evidence_items")
public class ForensicEvidenceItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="item_description") private String itemDescription;
  @Column(name="seal_no") private String sealNo;
  @Column(nullable=false) private String status = "stored";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ForensicEvidenceItem() {}
  public ForensicEvidenceItem(UUID facilityId, String governorate, UUID caseId, String itemDescription, String sealNo) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.itemDescription=itemDescription; this.sealNo=sealNo;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
