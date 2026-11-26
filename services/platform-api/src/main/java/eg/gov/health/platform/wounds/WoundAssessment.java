package eg.gov.health.platform.wounds;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "wound_assessments")
public class WoundAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="length_cm") private Double lengthCm;
  @Column(name="width_cm") private Double widthCm;
  @Column(name="depth_cm") private Double depthCm;
  @Column(name="area_cm2") private Double areaCm2;
  @Column(name="tissue_type") private String tissueType;
  private String exudate;
  @Column(name="pain_score") private Integer painScore;
  @Column(name="infection_suspected", nullable=false) private boolean infectionSuspected = false;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="assessed_at") private OffsetDateTime assessedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected WoundAssessment() {}
  public WoundAssessment(UUID facilityId, String governorate, UUID caseId, Double lengthCm, Double widthCm, Double depthCm,
                         String tissueType, String exudate, Integer painScore, boolean infectionSuspected, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.lengthCm=lengthCm; this.widthCm=widthCm;
    this.depthCm=depthCm; this.areaCm2=WoundRules.areaCm2(lengthCm, widthCm); this.tissueType=tissueType; this.exudate=exudate;
    this.painScore=painScore; this.infectionSuspected=infectionSuspected; this.assessedBy=assessedBy;
  }
  public UUID getId(){return id;} public Double getAreaCm2(){return areaCm2;} public boolean isInfectionSuspected(){return infectionSuspected;}
}
