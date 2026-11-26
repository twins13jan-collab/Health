package eg.gov.health.platform.palliative;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "advance_care_documents")
public class AdvanceCareDocument {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="document_type", nullable=false) private String documentType;
  @Column(nullable=false) private int version = 1;
  @Column(name="legal_verified", nullable=false) private boolean legalVerified = false;
  private String scope;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AdvanceCareDocument() {}
  public AdvanceCareDocument(UUID facilityId, String governorate, UUID patientId, String documentType, int version, boolean legalVerified, String scope) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.documentType=documentType;
    this.version=version; this.legalVerified=legalVerified; this.scope=scope;
  }
  public UUID getId(){return id;} public boolean isLegalVerified(){return legalVerified;} public String getStatus(){return status;}
  public String getDocumentType(){return documentType;}
  public void supersede(){ this.status="superseded"; }
}
