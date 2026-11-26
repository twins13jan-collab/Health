package eg.gov.health.platform.nfr;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nfr_requirements")
public class NfrRequirement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_ref") private String serviceRef;
  private String category; private String metric;
  @Column(name="target_value") private Double targetValue;
  private String comparison;
  @Column(nullable=false) private boolean critical = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NfrRequirement() {}
  public NfrRequirement(UUID facilityId, String governorate, String serviceRef, String category, String metric, Double targetValue, String comparison, boolean critical) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceRef=serviceRef; this.category=category; this.metric=metric;
    this.targetValue=targetValue; this.comparison=comparison; this.critical=critical;
  }
  public UUID getId(){return id;} public Double getTargetValue(){return targetValue;} public String getComparison(){return comparison;} public boolean isCritical(){return critical;}
}
