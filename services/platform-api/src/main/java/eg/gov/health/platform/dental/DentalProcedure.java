package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dental_procedures")
public class DentalProcedure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="plan_id") private UUID planId;
  @Column(name="tooth_number", nullable=false) private int toothNumber;
  @Column(name="site_verified", nullable=false) private boolean siteVerified = false;
  @Column(name="procedure_code", nullable=false) private String procedureCode;
  @Column(name="anesthesia_type") private String anesthesiaType;
  @Column(name="anesthesia_risk", nullable=false) private boolean anesthesiaRisk = false;
  @Column(name="physician_reviewed", nullable=false) private boolean physicianReviewed = false;
  private String materials;
  @Column(name="material_lot") private String materialLot;
  @Column(name="sterilization_cycle_id") private UUID sterilizationCycleId;
  private String dentist; private String complications;
  @Column(name="discharge_instructions") private String dischargeInstructions;
  @Column(nullable=false) private String status = "performed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DentalProcedure() {}
  public DentalProcedure(UUID facilityId, String governorate, UUID patientId, UUID planId, int toothNumber, boolean siteVerified,
                         String procedureCode, String anesthesiaType, boolean anesthesiaRisk, boolean physicianReviewed,
                         String materials, String materialLot, UUID sterilizationCycleId, String dentist,
                         String complications, String dischargeInstructions, String status) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.planId=planId;
    this.toothNumber=toothNumber; this.siteVerified=siteVerified; this.procedureCode=procedureCode;
    this.anesthesiaType=anesthesiaType; this.anesthesiaRisk=anesthesiaRisk; this.physicianReviewed=physicianReviewed;
    this.materials=materials; this.materialLot=materialLot; this.sterilizationCycleId=sterilizationCycleId;
    this.dentist=dentist; this.complications=complications; this.dischargeInstructions=dischargeInstructions; this.status=status;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
}
