package eg.gov.health.platform.facilityreadiness;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "readiness_assessments")
public class ReadinessAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_ref") private String serviceRef;
  @Column(name="licences_ok", nullable=false) private boolean licencesOk = false;
  @Column(name="staff_ok", nullable=false) private boolean staffOk = false;
  @Column(name="equipment_ok", nullable=false) private boolean equipmentOk = false;
  @Column(name="stock_ok", nullable=false) private boolean stockOk = false;
  @Column(name="infrastructure_ok", nullable=false) private boolean infrastructureOk = false;
  @Column(name="protocols_ok", nullable=false) private boolean protocolsOk = false;
  @Column(name="training_ok", nullable=false) private boolean trainingOk = false;
  @Column(name="referral_paths_ok", nullable=false) private boolean referralPathsOk = false;
  @Column(name="remediation_plan") private String remediationPlan;
  @Column(name="decided_by") private String decidedBy;
  private String decision;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ReadinessAssessment() {}
  public ReadinessAssessment(UUID facilityId, String governorate, String serviceRef, boolean licencesOk, boolean staffOk, boolean equipmentOk,
                                 boolean stockOk, boolean infrastructureOk, boolean protocolsOk, boolean trainingOk, boolean referralPathsOk,
                                 String remediationPlan) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceRef=serviceRef; this.licencesOk=licencesOk; this.staffOk=staffOk;
    this.equipmentOk=equipmentOk; this.stockOk=stockOk; this.infrastructureOk=infrastructureOk; this.protocolsOk=protocolsOk;
    this.trainingOk=trainingOk; this.referralPathsOk=referralPathsOk; this.remediationPlan=remediationPlan;
  }
  public UUID getId(){return id;} public boolean isLicencesOk(){return licencesOk;} public boolean isStaffOk(){return staffOk;}
  public boolean isEquipmentOk(){return equipmentOk;} public boolean isStockOk(){return stockOk;}
  public boolean isInfrastructureOk(){return infrastructureOk;} public boolean isProtocolsOk(){return protocolsOk;}
  public boolean isTrainingOk(){return trainingOk;} public boolean isReferralPathsOk(){return referralPathsOk;}
  public String getRemediationPlan(){return remediationPlan;} public String getDecision(){return decision;}
  public void decide(String decision, String decidedBy){ this.decision=decision; this.decidedBy=decidedBy; this.updatedAt=OffsetDateTime.now(); }
}
