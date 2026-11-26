package eg.gov.health.platform.accreditation;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "licence_files")
public class LicenceFile {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String scope;
  @Column(name="holder_ref") private String holderRef;
  @Column(name="standard_ref") private String standardRef;
  @Column(name="issued_at") private LocalDate issuedAt;
  @Column(name="expires_at") private LocalDate expiresAt;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LicenceFile() {}
  public LicenceFile(UUID facilityId, String governorate, String scope, String holderRef, String standardRef, LocalDate issuedAt, LocalDate expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.scope=scope; this.holderRef=holderRef; this.standardRef=standardRef;
    this.issuedAt=issuedAt; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public LocalDate getExpiresAt(){return expiresAt;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
