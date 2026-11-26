package eg.gov.health.platform.followup;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "followup_plans")
public class FollowUpPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="discharge_ref") private String dischargeRef;
  private String responsible; @Column(name="plan_summary") private String planSummary;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FollowUpPlan() {}
  public FollowUpPlan(UUID facilityId, String governorate, String patientRef, String dischargeRef, String responsible, String planSummary) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.dischargeRef=dischargeRef;
    this.responsible=responsible; this.planSummary=planSummary;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
