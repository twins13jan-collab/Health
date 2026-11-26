package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "bank_statements")
public class BankStatement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_code", nullable=false) private String accountCode;
  @Column(name="statement_ref", nullable=false) private String statementRef;
  @Column(name="period_key") private String periodKey;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected BankStatement() {}
  public BankStatement(UUID facilityId, String governorate, String accountCode, String statementRef, String periodKey) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountCode=accountCode; this.statementRef=statementRef; this.periodKey=periodKey;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;}
}
