package eg.gov.health.platform.clinicalquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "quality_indicators")
public class QualityIndicator {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String category; private Double value;
  @Column(name="case_mix_adjusted", nullable=false) private boolean caseMixAdjusted = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected QualityIndicator() {}
  public QualityIndicator(UUID facilityId, String governorate, String name, String category, Double value, boolean caseMixAdjusted) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.category=category; this.value=value; this.caseMixAdjusted=caseMixAdjusted;
  }
  public UUID getId(){return id;} public boolean isCaseMixAdjusted(){return caseMixAdjusted;}
}
