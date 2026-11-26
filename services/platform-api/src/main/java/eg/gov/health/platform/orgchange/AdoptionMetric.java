package eg.gov.health.platform.orgchange;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "adoption_metrics")
public class AdoptionMetric {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="initiative_id", nullable=false) private UUID initiativeId;
  @Column(name="active_users_pct") private BigDecimal activeUsersPct;
  @Column(name="workaround_reports") private Integer workaroundReports;
  @Column(name="measured_at") private OffsetDateTime measuredAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AdoptionMetric() {}
  public AdoptionMetric(UUID facilityId, String governorate, UUID initiativeId, BigDecimal activeUsersPct, Integer workaroundReports) {
    this.facilityId=facilityId; this.governorate=governorate; this.initiativeId=initiativeId; this.activeUsersPct=activeUsersPct;
    this.workaroundReports=workaroundReports; this.measuredAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
