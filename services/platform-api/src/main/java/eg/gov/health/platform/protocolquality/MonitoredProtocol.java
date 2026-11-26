package eg.gov.health.platform.protocolquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "monitored_protocols")
public class MonitoredProtocol {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="source_ref") private String sourceRef;
  @Column(name="approving_body") private String approvingBody;
  private String scope; private String contraindications; private String version;
  @Column(name="review_due") private LocalDate reviewDue;
  @Column(nullable=false) private String status = "current";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MonitoredProtocol() {}
  public MonitoredProtocol(UUID facilityId, String governorate, String name, String sourceRef, String approvingBody, String scope,
                           String contraindications, String version, LocalDate reviewDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.sourceRef=sourceRef; this.approvingBody=approvingBody;
    this.scope=scope; this.contraindications=contraindications; this.version=version; this.reviewDue=reviewDue;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public LocalDate getReviewDue(){return reviewDue;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
