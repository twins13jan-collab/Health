package eg.gov.health.platform.protocolversions;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinical_protocol_versions")
public class ClinicalProtocolVersion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String code; private String title; private String version;
  @Column(name="scientific_ref") private String scientificRef;
  @Column(name="owner_body") private String ownerBody;
  @Column(name="committee_approved", nullable=false) private boolean committeeApproved = false;
  @Column(name="effective_from") private LocalDate effectiveFrom;
  @Column(name="review_due") private LocalDate reviewDue;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ClinicalProtocolVersion() {}
  public ClinicalProtocolVersion(UUID facilityId, String governorate, String code, String title, String version, String scientificRef,
                                     String ownerBody, boolean committeeApproved, LocalDate effectiveFrom, LocalDate reviewDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.code=code; this.title=title; this.version=version;
    this.scientificRef=scientificRef; this.ownerBody=ownerBody; this.committeeApproved=committeeApproved;
    this.effectiveFrom=effectiveFrom; this.reviewDue=reviewDue;
  }
  public UUID getId(){return id;} public String getScientificRef(){return scientificRef;} public String getOwnerBody(){return ownerBody;}
  public boolean isCommitteeApproved(){return committeeApproved;} public LocalDate getEffectiveFrom(){return effectiveFrom;}
  public String getStatus(){return status;} public String getVersion(){return version;}
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
  public void withdraw(){ this.status="withdrawn"; this.updatedAt=OffsetDateTime.now(); }
}
