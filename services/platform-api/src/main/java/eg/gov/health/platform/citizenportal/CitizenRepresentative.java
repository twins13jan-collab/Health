package eg.gov.health.platform.citizenportal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "citizen_representatives")
public class CitizenRepresentative {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_id", nullable=false) private UUID accountId;
  @Column(name="representative_name") private String representativeName;
  private String relationship; private String scope;
  @Column(name="valid_until") private LocalDate validUntil;
  @Column(nullable=false) private boolean approved = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CitizenRepresentative() {}
  public CitizenRepresentative(UUID facilityId, String governorate, UUID accountId, String representativeName, String relationship, String scope, LocalDate validUntil, boolean approved) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountId=accountId; this.representativeName=representativeName;
    this.relationship=relationship; this.scope=scope; this.validUntil=validUntil; this.approved=approved;
  }
  public UUID getId(){return id;} public boolean isApproved(){return approved;} public LocalDate getValidUntil(){return validUntil;} public String getScope(){return scope;}
}
