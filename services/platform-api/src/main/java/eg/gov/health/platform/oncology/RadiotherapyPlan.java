package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "radiotherapy_plans")
public class RadiotherapyPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="total_dose") private String totalDose;
  @Column(name="fractions_planned", nullable=false) private int fractionsPlanned = 0;
  @Column(name="organs_at_risk") private String organsAtRisk;
  @Column(name="approved_by") private String approvedBy;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RadiotherapyPlan() {}
  public RadiotherapyPlan(UUID facilityId, String governorate, UUID caseId, String totalDose, int fractionsPlanned, String organsAtRisk, String approvedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.totalDose=totalDose;
    this.fractionsPlanned=fractionsPlanned; this.organsAtRisk=organsAtRisk; this.approvedBy=approvedBy;
    this.status = approvedBy != null ? "approved" : "planned";
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;}
}
