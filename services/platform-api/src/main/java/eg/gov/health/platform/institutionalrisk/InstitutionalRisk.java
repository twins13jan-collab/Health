package eg.gov.health.platform.institutionalrisk;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "institutional_risks")
public class InstitutionalRisk {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String category; private String title;
  private Integer likelihood; private Integer impact; private String controls;
  @Column(name="owner_ref") private String ownerRef;
  @Column(name="treatment_plan") private String treatmentPlan;
  @Column(name="residual_likelihood") private Integer residualLikelihood;
  @Column(name="residual_impact") private Integer residualImpact;
  @Column(name="accepted_by") private String acceptedBy;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InstitutionalRisk() {}
  public InstitutionalRisk(UUID facilityId, String governorate, String category, String title, Integer likelihood, Integer impact, String controls,
                           String ownerRef, String treatmentPlan, Integer residualLikelihood, Integer residualImpact, String acceptedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.category=category; this.title=title; this.likelihood=likelihood; this.impact=impact;
    this.controls=controls; this.ownerRef=ownerRef; this.treatmentPlan=treatmentPlan; this.residualLikelihood=residualLikelihood;
    this.residualImpact=residualImpact; this.acceptedBy=acceptedBy;
  }
  public UUID getId(){return id;} public String getOwnerRef(){return ownerRef;} public String getTreatmentPlan(){return treatmentPlan;}
  public Integer getResidualLikelihood(){return residualLikelihood;} public Integer getResidualImpact(){return residualImpact;} public String getAcceptedBy(){return acceptedBy;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
