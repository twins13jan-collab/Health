package eg.gov.health.platform.clinicalai;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ai_model_incidents")
public class AiModelIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="model_id", nullable=false) private UUID modelId;
  @Column(nullable=false) private String kind;
  private String description; @Column(name="drift_metric") private java.math.BigDecimal driftMetric;
  private String action; private String investigation;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AiModelIncident() {}
  public AiModelIncident(UUID facilityId, String governorate, UUID modelId, String kind, String description, java.math.BigDecimal driftMetric, String action) {
    this.facilityId=facilityId; this.governorate=governorate; this.modelId=modelId; this.kind=kind; this.description=description;
    this.driftMetric=driftMetric; this.action=action;
    if ("suspended".equals(action)) this.status="suspended";
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void close(String investigation, String status){ this.investigation=investigation; this.status=status; this.updatedAt=OffsetDateTime.now(); }
}
