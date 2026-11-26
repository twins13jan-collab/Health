package eg.gov.health.platform.legalcompliance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "compliance_exceptions")
public class ComplianceException {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="requirement_id", nullable=false) private UUID requirementId;
  private String justification;
  @Column(name="authority_basis") private String authorityBasis;
  @Column(name="expires_at") private LocalDate expiresAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ComplianceException() {}
  public ComplianceException(UUID facilityId, String governorate, UUID requirementId, String justification, String authorityBasis, LocalDate expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.requirementId=requirementId; this.justification=justification;
    this.authorityBasis=authorityBasis; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;}
}
