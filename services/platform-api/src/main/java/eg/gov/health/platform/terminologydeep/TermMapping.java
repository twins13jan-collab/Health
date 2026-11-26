package eg.gov.health.platform.terminologydeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "term_mappings")
public class TermMapping {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="local_term") private String localTerm;
  @Column(name="standard_system") private String standardSystem;
  @Column(name="standard_code") private String standardCode;
  @Column(name="meaning_verified", nullable=false) private boolean meaningVerified = false;
  @Column(name="specialist_reviewed", nullable=false) private boolean specialistReviewed = false;
  @Column(nullable=false) private boolean supported = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TermMapping() {}
  public TermMapping(UUID facilityId, String governorate, String localTerm, String standardSystem, String standardCode,
                       boolean meaningVerified, boolean specialistReviewed, boolean supported) {
    this.facilityId=facilityId; this.governorate=governorate; this.localTerm=localTerm; this.standardSystem=standardSystem;
    this.standardCode=standardCode; this.meaningVerified=meaningVerified; this.specialistReviewed=specialistReviewed; this.supported=supported;
  }
  public UUID getId(){return id;} public boolean isMeaningVerified(){return meaningVerified;} public boolean isSpecialistReviewed(){return specialistReviewed;}
  public boolean isSupported(){return supported;}
}
