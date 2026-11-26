package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "chart_of_accounts")
public class ChartOfAccount {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_code", nullable=false) private String accountCode;
  @Column(nullable=false) private String name;
  @Column(nullable=false) private String classification;
  @Column(name="posting_rules") private String postingRules;
  private String owner;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ChartOfAccount() {}
  public ChartOfAccount(UUID facilityId, String governorate, String accountCode, String name, String classification, String postingRules, String owner) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountCode=accountCode; this.name=name;
    this.classification=classification; this.postingRules=postingRules; this.owner=owner;
  }
  public UUID getId(){return id;}
}
