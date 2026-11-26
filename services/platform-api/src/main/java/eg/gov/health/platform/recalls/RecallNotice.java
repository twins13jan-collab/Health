package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "recall_notices")
public class RecallNotice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String authority;
  @Column(nullable=false) private String reason;
  @Column(nullable=false) private String severity;
  @Column(name="effective_date", nullable=false) private LocalDate effectiveDate;
  @Column(name="published_by") private String publishedBy;
  @Column(name="regulator_ack", nullable=false) private boolean regulatorAck = false;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RecallNotice() {}
  public RecallNotice(UUID facilityId, String governorate, String authority, String reason, String severity, LocalDate effectiveDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.authority=authority; this.reason=reason;
    this.severity=severity; this.effectiveDate=effectiveDate;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;} public boolean isRegulatorAck(){return regulatorAck;}
  public void publish(String by){ this.publishedBy=by; this.status="published"; this.updatedAt=OffsetDateTime.now(); }
  public void ack(){ this.regulatorAck=true; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
