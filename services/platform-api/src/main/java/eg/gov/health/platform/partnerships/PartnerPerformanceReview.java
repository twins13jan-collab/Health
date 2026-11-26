package eg.gov.health.platform.partnerships;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "partner_performance_reviews")
public class PartnerPerformanceReview {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="partner_id", nullable=false) private UUID partnerId;
  private String period;
  @Column(name="sla_score") private BigDecimal slaScore;
  @Column(name="incidents_count") private Integer incidentsCount;
  @Column(name="audit_result") private String auditResult;
  @Column(name="reviewed_by") private String reviewedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PartnerPerformanceReview() {}
  public PartnerPerformanceReview(UUID facilityId, String governorate, UUID partnerId, String period, BigDecimal slaScore, Integer incidentsCount, String auditResult, String reviewedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.partnerId=partnerId; this.period=period; this.slaScore=slaScore;
    this.incidentsCount=incidentsCount; this.auditResult=auditResult; this.reviewedBy=reviewedBy;
  }
  public UUID getId(){return id;}
}
