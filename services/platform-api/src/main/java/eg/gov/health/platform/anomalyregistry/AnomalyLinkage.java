package eg.gov.health.platform.anomalyregistry;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "anomaly_linkages")
public class AnomalyLinkage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="link_type") private String linkType;
  @Column(name="linked_ref") private String linkedRef;
  @Column(name="linked_by") private String linkedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AnomalyLinkage() {}
  public AnomalyLinkage(UUID facilityId, String governorate, UUID caseId, String linkType, String linkedRef, String linkedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.linkType=linkType; this.linkedRef=linkedRef; this.linkedBy=linkedBy;
  }
  public UUID getId(){return id;}
}
