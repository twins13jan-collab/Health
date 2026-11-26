package eg.gov.health.platform.clinicalriskmatrix;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "matrix_alert_rules")
public class MatrixAlertRule {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="matrix_risk_id", nullable=false) private UUID matrixRiskId;
  @Column(name="alert_name") private String alertName;
  @Column(name="total_fired") private Integer totalFired;
  @Column(name="false_positive_count") private Integer falsePositiveCount;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MatrixAlertRule() {}
  public MatrixAlertRule(UUID facilityId, String governorate, UUID matrixRiskId, String alertName, Integer totalFired, Integer falsePositiveCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.matrixRiskId=matrixRiskId; this.alertName=alertName;
    this.totalFired=totalFired; this.falsePositiveCount=falsePositiveCount;
  }
  public UUID getId(){return id;} public Integer getTotalFired(){return totalFired;} public Integer getFalsePositiveCount(){return falsePositiveCount;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
