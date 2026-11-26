package eg.gov.health.platform.followup;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "high_risk_medication_checks")
public class HighRiskMedicationCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  private String medication; @Column(name="check_type") private String checkType;
  private String finding; private String action;
  @Column(name="checked_by") private String checkedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HighRiskMedicationCheck() {}
  public HighRiskMedicationCheck(UUID facilityId, String governorate, UUID planId, String medication, String checkType, String finding, String action, String checkedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.medication=medication; this.checkType=checkType;
    this.finding=finding; this.action=action; this.checkedBy=checkedBy;
  }
  public UUID getId(){return id;}
}
