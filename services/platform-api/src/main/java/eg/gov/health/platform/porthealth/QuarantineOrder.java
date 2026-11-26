package eg.gov.health.platform.porthealth;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "quarantine_orders")
public class QuarantineOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="report_id", nullable=false) private UUID reportId;
  @Column(name="facility_ref") private String facilityRef;
  @Column(name="legal_basis") private String legalBasis;
  @Column(name="starts_at") private OffsetDateTime startsAt;
  @Column(name="ends_at") private OffsetDateTime endsAt;
  @Column(name="released_by") private String releasedBy;
  @Column(name="release_reason") private String releaseReason;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected QuarantineOrder() {}
  public QuarantineOrder(UUID facilityId, String governorate, UUID reportId, String facilityRef, String legalBasis, OffsetDateTime startsAt, OffsetDateTime endsAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.reportId=reportId; this.facilityRef=facilityRef; this.legalBasis=legalBasis;
    this.startsAt=startsAt; this.endsAt=endsAt;
  }
  public UUID getId(){return id;} public OffsetDateTime getEndsAt(){return endsAt;} public String getStatus(){return status;}
  public void complete(){ this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
  public void releaseEarly(String by, String reason){ this.releasedBy=by; this.releaseReason=reason; this.status="released_early"; this.updatedAt=OffsetDateTime.now(); }
}
