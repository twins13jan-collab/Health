package eg.gov.health.platform.anomalyregistry;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "anomaly_cases")
public class AnomalyCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_code", nullable=false) private String caseCode;
  @Column(name="newborn_ref") private String newbornRef;
  @Column(name="anomaly_code") private String anomalyCode;
  private String classification;
  @Column(name="source_type") private String sourceType;
  @Column(name="reported_by") private String reportedBy;
  @Column(nullable=false) private String status = "registered";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AnomalyCase() {}
  public AnomalyCase(UUID facilityId, String governorate, String caseCode, String newbornRef, String anomalyCode, String classification, String sourceType, String reportedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseCode=caseCode; this.newbornRef=newbornRef; this.anomalyCode=anomalyCode;
    this.classification=classification; this.sourceType=sourceType; this.reportedBy=reportedBy;
  }
  public UUID getId(){return id;}
}
