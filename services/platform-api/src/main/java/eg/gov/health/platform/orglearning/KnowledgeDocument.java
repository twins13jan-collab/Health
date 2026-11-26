package eg.gov.health.platform.orglearning;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "knowledge_documents")
public class KnowledgeDocument {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title;
  @Column(name="owner_ref") private String ownerRef;
  private String version; private String audience;
  @Column(name="effective_from") private LocalDate effectiveFrom;
  @Column(name="review_due") private LocalDate reviewDue;
  @Column(nullable=false) private boolean mandatory = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected KnowledgeDocument() {}
  public KnowledgeDocument(UUID facilityId, String governorate, String title, String ownerRef, String version, String audience,
                             LocalDate effectiveFrom, LocalDate reviewDue, boolean mandatory) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.ownerRef=ownerRef; this.version=version;
    this.audience=audience; this.effectiveFrom=effectiveFrom; this.reviewDue=reviewDue; this.mandatory=mandatory;
  }
  public UUID getId(){return id;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getReviewDue(){return reviewDue;}
}
