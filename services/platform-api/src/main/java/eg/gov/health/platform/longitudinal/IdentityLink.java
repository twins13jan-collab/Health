package eg.gov.health.platform.longitudinal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "identity_links")
public class IdentityLink {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="identity_id", nullable=false) private UUID identityId;
  @Column(name="link_type", nullable=false) private String linkType;
  private String issuer; @Column(name="protected_value") private String protectedValue;
  @Column(name="valid_from") private LocalDate validFrom; @Column(name="valid_to") private LocalDate validTo;
  @Column(name="link_status", nullable=false) private String linkStatus = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IdentityLink() {}
  public IdentityLink(UUID facilityId, String governorate, UUID identityId, String linkType, String issuer, String protectedValue, LocalDate validFrom, LocalDate validTo) {
    this.facilityId=facilityId; this.governorate=governorate; this.identityId=identityId; this.linkType=linkType;
    this.issuer=issuer; this.protectedValue=protectedValue; this.validFrom=validFrom; this.validTo=validTo;
  }
  public UUID getId(){return id;}
}
