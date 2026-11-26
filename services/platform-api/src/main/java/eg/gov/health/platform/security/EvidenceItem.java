package eg.gov.health.platform.security;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "evidence_items")
public class EvidenceItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(nullable=false) private String kind;
  private String description;
  @Column(name="sha256_hash", nullable=false) private String sha256Hash;
  @Column(name="custody_chain", nullable=false) private String custodyChain = "[]";
  @Column(name="collected_by") private String collectedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EvidenceItem() {}
  public EvidenceItem(UUID facilityId, String governorate, UUID incidentId, String kind, String description, String sha256Hash, String collectedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.kind=kind;
    this.description=description; this.sha256Hash=sha256Hash; this.collectedBy=collectedBy;
    this.custodyChain="[\"collected by " + (collectedBy==null?"unknown":collectedBy) + "\"]";
  }
  public UUID getId(){return id;} public String getCustodyChain(){return custodyChain;}
  /** Custody is append-only: transfers are added, history never rewritten. */
  public void appendCustody(String entryJson) {
    String chain = this.custodyChain;
    this.custodyChain = chain.substring(0, chain.length()-1) + (chain.length()>2 ? "," : "") + entryJson + "]";
  }
}
