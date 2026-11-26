package eg.gov.health.platform.masterdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "data_quality_rules")
public class DataQualityRule {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="rule_type", nullable=false) private String ruleType;
  @Column(nullable=false) private String target;
  private String expression; private java.math.BigDecimal threshold;
  @Column(name="unknown_rate") private java.math.BigDecimal unknownRate;
  @Column(name="duplicate_rate") private java.math.BigDecimal duplicateRate;
  @Column(name="conflict_rate") private java.math.BigDecimal conflictRate;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DataQualityRule() {}
  public DataQualityRule(UUID facilityId, String governorate, String ruleType, String target, String expression, java.math.BigDecimal threshold,
                         java.math.BigDecimal unknownRate, java.math.BigDecimal duplicateRate, java.math.BigDecimal conflictRate) {
    this.facilityId=facilityId; this.governorate=governorate; this.ruleType=ruleType; this.target=target; this.expression=expression;
    this.threshold=threshold; this.unknownRate=unknownRate; this.duplicateRate=duplicateRate; this.conflictRate=conflictRate;
  }
  public UUID getId(){return id;}
}
