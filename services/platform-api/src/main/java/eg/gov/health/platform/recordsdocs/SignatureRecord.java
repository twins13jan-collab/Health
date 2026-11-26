package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "signature_records")
public class SignatureRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="document_id", nullable=false) private UUID documentId;
  @Column(nullable=false) private int version;
  @Column(name="version_hash") private String versionHash;
  private String signer; @Column(name="certificate_ref") private String certificateRef;
  @Column(name="certificate_valid", nullable=false) private boolean certificateValid = false;
  private String scope; @Column(name="signed_at") private OffsetDateTime signedAt;
  @Column(nullable=false) private String status = "recorded";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SignatureRecord() {}
  public SignatureRecord(UUID facilityId, String governorate, UUID documentId, int version, String versionHash, String signer, String certificateRef, boolean certificateValid, String scope) {
    this.facilityId=facilityId; this.governorate=governorate; this.documentId=documentId; this.version=version; this.versionHash=versionHash;
    this.signer=signer; this.certificateRef=certificateRef; this.certificateValid=certificateValid; this.scope=scope; this.signedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getVersionHash(){return versionHash;}
  public void validate(boolean counts){ this.status = counts ? "valid" : "invalid"; }
  public void supersede(){ this.status="superseded"; }
}
