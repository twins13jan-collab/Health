package eg.gov.health.platform.clinicalcosting;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pathway_cost_cases")
public class PathwayCostCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="pathway_ref") private String pathwayRef;
  @Column(name="standard_cost") private Double standardCost;
  @Column(name="variance_review_status", nullable=false) private String varianceReviewStatus = "none";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PathwayCostCase() {}
  public PathwayCostCase(UUID facilityId, String governorate, String patientRef, String pathwayRef, Double standardCost) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.pathwayRef=pathwayRef; this.standardCost=standardCost;
  }
  public UUID getId(){return id;} public Double getStandardCost(){return standardCost;} public String getVarianceReviewStatus(){return varianceReviewStatus;}
  public void setVarianceReviewStatus(String s){ this.varianceReviewStatus=s; this.updatedAt=OffsetDateTime.now(); }
}
