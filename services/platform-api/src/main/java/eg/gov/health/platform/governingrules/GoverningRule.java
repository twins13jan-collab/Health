package eg.gov.health.platform.governingrules;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "governing_rules")
public class GoverningRule {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="rule_code") private String ruleCode;
  private String title; private String description;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected GoverningRule() {}
  public GoverningRule(UUID facilityId, String governorate, String ruleCode, String title, String description) {
    this.facilityId=facilityId; this.governorate=governorate; this.ruleCode=ruleCode; this.title=title; this.description=description;
  }
  public UUID getId(){return id;} public String getRuleCode(){return ruleCode;}
}
