package eg.gov.health.platform.transfersafety;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "transfer_assessments")
public class TransferAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="patient_stable") private Boolean patientStable;
  @Column(name="stability_confirmed", nullable=false) private boolean stabilityConfirmed = false;
  @Column(name="escort_level") private String escortLevel;
  @Column(name="equipment_needed") private String equipmentNeeded;
  @Column(name="medications_needed") private String medicationsNeeded;
  @Column(name="receiving_facility") private String receivingFacility;
  @Column(name="receiving_acceptance_confirmed", nullable=false) private boolean receivingAcceptanceConfirmed = false;
  @Column(name="receiving_capacity_confirmed", nullable=false) private boolean receivingCapacityConfirmed = false;
  @Column(name="critical_results_summary") private String criticalResultsSummary;
  @Column(nullable=false) private String status = "assessed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TransferAssessment() {}
  public TransferAssessment(UUID facilityId, String governorate, String patientRef, Boolean patientStable, String escortLevel,
                            String equipmentNeeded, String medicationsNeeded, String receivingFacility, String criticalResultsSummary) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.patientStable=patientStable; this.escortLevel=escortLevel;
    this.equipmentNeeded=equipmentNeeded; this.medicationsNeeded=medicationsNeeded; this.receivingFacility=receivingFacility;
    this.criticalResultsSummary=criticalResultsSummary;
  }
  public UUID getId(){return id;} public Boolean getPatientStable(){return patientStable;} public String getEscortLevel(){return escortLevel;}
  public boolean isStabilityConfirmed(){return stabilityConfirmed;} public boolean isReceivingAcceptanceConfirmed(){return receivingAcceptanceConfirmed;}
  public boolean isReceivingCapacityConfirmed(){return receivingCapacityConfirmed;} public String getStatus(){return status;}
  public void confirmStability(){ this.stabilityConfirmed=true; this.updatedAt=OffsetDateTime.now(); }
  public void confirmAcceptance(){ this.receivingAcceptanceConfirmed=true; this.updatedAt=OffsetDateTime.now(); }
  public void confirmCapacity(){ this.receivingCapacityConfirmed=true; this.updatedAt=OffsetDateTime.now(); }
  public void depart(){ this.status="departed"; this.updatedAt=OffsetDateTime.now(); }
  public void complete(){ this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
