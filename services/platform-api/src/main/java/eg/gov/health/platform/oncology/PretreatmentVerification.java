package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pretreatment_verifications")
public class PretreatmentVerification {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="cycle_id", nullable=false) private UUID cycleId;
  @Column(name="weight_kg") private java.math.BigDecimal weightKg;
  private java.math.BigDecimal bsa;
  @Column(name="labs_summary") private String labsSummary;
  @Column(name="labs_within_conditions", nullable=false) private boolean labsWithinConditions = false;
  @Column(name="weight_change_pct") private java.math.BigDecimal weightChangePct;
  @Column(name="allergy_checked", nullable=false) private boolean allergyChecked = false;
  @Column(name="consent_confirmed", nullable=false) private boolean consentConfirmed = false;
  private String verifier; @Column(name="pharmacy_review") private String pharmacyReview;
  @Column(nullable=false) private boolean suspended = false;
  @Column(name="suspend_reason") private String suspendReason;
  @Column(nullable=false) private String status = "pending";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PretreatmentVerification() {}
  public PretreatmentVerification(UUID facilityId, String governorate, UUID cycleId, java.math.BigDecimal weightKg, java.math.BigDecimal bsa,
                                  String labsSummary, boolean labsWithinConditions, java.math.BigDecimal weightChangePct,
                                  boolean allergyChecked, boolean consentConfirmed, String verifier, String pharmacyReview,
                                  boolean suspended, String suspendReason) {
    this.facilityId=facilityId; this.governorate=governorate; this.cycleId=cycleId; this.weightKg=weightKg; this.bsa=bsa;
    this.labsSummary=labsSummary; this.labsWithinConditions=labsWithinConditions; this.weightChangePct=weightChangePct;
    this.allergyChecked=allergyChecked; this.consentConfirmed=consentConfirmed; this.verifier=verifier; this.pharmacyReview=pharmacyReview;
    this.suspended=suspended; this.suspendReason=suspendReason; this.status = suspended ? "suspended" : "verified";
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public boolean isSuspended(){return suspended;}
}
