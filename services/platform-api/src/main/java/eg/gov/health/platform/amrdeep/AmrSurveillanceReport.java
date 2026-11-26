package eg.gov.health.platform.amrdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "amr_surveillance_reports")
public class AmrSurveillanceReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String organism; private String antibiotic;
  @Column(name="resistance_rate") private Double resistanceRate;
  @Column(name="baseline_rate") private Double baselineRate;
  @Column(name="pattern_verified", nullable=false) private boolean patternVerified = false;
  @Column(name="identity_protected", nullable=false) private boolean identityProtected = false;
  @Column(nullable=false) private boolean published = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AmrSurveillanceReport() {}
  public AmrSurveillanceReport(UUID facilityId, String governorate, String organism, String antibiotic, Double resistanceRate,
                                 Double baselineRate, boolean identityProtected) {
    this.facilityId=facilityId; this.governorate=governorate; this.organism=organism; this.antibiotic=antibiotic;
    this.resistanceRate=resistanceRate; this.baselineRate=baselineRate; this.identityProtected=identityProtected;
  }
  public UUID getId(){return id;} public Double getResistanceRate(){return resistanceRate;} public Double getBaselineRate(){return baselineRate;}
  public boolean isIdentityProtected(){return identityProtected;} public boolean isPatternVerified(){return patternVerified;}
  public void verifyPattern(){ this.patternVerified=true; }
  public void publish(){ this.published=true; }
}
