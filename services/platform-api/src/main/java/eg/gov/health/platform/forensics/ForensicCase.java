package eg.gov.health.platform.forensics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "forensic_cases")
public class ForensicCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_ref") private String caseRef;
  @Column(name="requesting_authority") private String requestingAuthority;
  @Column(name="mandate_ref") private String mandateRef;
  @Column(name="authority_verified", nullable=false) private boolean authorityVerified = false;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ForensicCase() {}
  public ForensicCase(UUID facilityId, String governorate, String caseRef, String requestingAuthority, String mandateRef, boolean authorityVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseRef=caseRef; this.requestingAuthority=requestingAuthority;
    this.mandateRef=mandateRef; this.authorityVerified=authorityVerified;
  }
  public UUID getId(){return id;} public boolean isAuthorityVerified(){return authorityVerified;} public String getRequestingAuthority(){return requestingAuthority;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
