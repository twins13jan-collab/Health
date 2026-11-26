package eg.gov.health.platform.unknownpatient;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "identity_verification_chain")
public class IdentityVerificationChain {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="temp_id", nullable=false) private UUID tempId;
  private String step; private String actor; private String detail;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IdentityVerificationChain() {}
  public IdentityVerificationChain(UUID facilityId, String governorate, UUID tempId, String step, String actor, String detail) {
    this.facilityId=facilityId; this.governorate=governorate; this.tempId=tempId; this.step=step; this.actor=actor; this.detail=detail;
  }
  public UUID getId(){return id;}
}
