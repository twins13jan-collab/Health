package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity(name = "ChemoAdministrationRecord") @Table(name = "chemo_administrations")
public class AdministrationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="cycle_id", nullable=false) private UUID cycleId;
  @Column(name="prescribed_dose") private String prescribedDose;
  @Column(name="administered_dose") private String administeredDose;
  private java.math.BigDecimal variance;
  @Column(name="variance_reason") private String varianceReason;
  @Column(name="variance_approved_by") private String varianceApprovedBy;
  @Column(name="drug_available", nullable=false) private boolean drugAvailable = true;
  @Column(nullable=false) private boolean recalled = false;
  @Column(name="substitute_drug") private String substituteDrug;
  @Column(name="substitute_decision_by") private String substituteDecisionBy;
  @Column(name="administered_by") private String administeredBy;
  @Column(name="identity_checked", nullable=false) private boolean identityChecked = false;
  @Column(name="batch_ref") private String batchRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AdministrationRecord() {}
  public AdministrationRecord(UUID facilityId, String governorate, UUID cycleId, String prescribedDose, String administeredDose,
                              java.math.BigDecimal variance, String varianceReason, String varianceApprovedBy, boolean drugAvailable,
                              boolean recalled, String substituteDrug, String substituteDecisionBy, String administeredBy,
                              boolean identityChecked, String batchRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.cycleId=cycleId; this.prescribedDose=prescribedDose;
    this.administeredDose=administeredDose; this.variance=variance; this.varianceReason=varianceReason; this.varianceApprovedBy=varianceApprovedBy;
    this.drugAvailable=drugAvailable; this.recalled=recalled; this.substituteDrug=substituteDrug; this.substituteDecisionBy=substituteDecisionBy;
    this.administeredBy=administeredBy; this.identityChecked=identityChecked; this.batchRef=batchRef;
  }
  public UUID getId(){return id;}
}
