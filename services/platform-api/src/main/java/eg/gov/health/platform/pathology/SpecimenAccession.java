package eg.gov.health.platform.pathology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "specimen_accessions")
public class SpecimenAccession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  @Column(name="accession_number", nullable=false) private String accessionNumber;
  @Column(name="container_type") private String containerType;
  private String fixative;
  @Column(name="collected_at") private OffsetDateTime collectedAt;
  @Column(name="received_at") private OffsetDateTime receivedAt = OffsetDateTime.now();
  @Column(name="identity_verified", nullable=false) private boolean identityVerified = false;
  @Column(name="insufficiency_note") private String insufficiencyNote;
  @Column(nullable=false) private String status;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SpecimenAccession() {}
  public SpecimenAccession(UUID facilityId, String governorate, UUID orderId, String accessionNumber, String containerType,
                           String fixative, OffsetDateTime collectedAt, boolean identityVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.accessionNumber=accessionNumber;
    this.containerType=containerType; this.fixative=fixative; this.collectedAt=collectedAt; this.identityVerified=identityVerified;
    this.status=PathologyRules.receiptStatus(identityVerified);
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public OffsetDateTime getReceivedAt(){return receivedAt;} public UUID getOrderId(){return orderId;}
  public void verifyIdentity(){ this.identityVerified=true; if("quarantined".equals(this.status)) this.status="received"; this.updatedAt=OffsetDateTime.now(); }
  public void accept(){ this.status="accepted"; this.updatedAt=OffsetDateTime.now(); }
  public void markInsufficient(String note){ this.status="insufficient"; this.insufficiencyNote=note; this.updatedAt=OffsetDateTime.now(); }
}
