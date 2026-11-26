package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "journal_lines")
public class JournalLine {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="journal_id", nullable=false) private UUID journalId;
  @Column(name="account_code", nullable=false) private String accountCode;
  @Column(nullable=false) private java.math.BigDecimal debit = java.math.BigDecimal.ZERO;
  @Column(nullable=false) private java.math.BigDecimal credit = java.math.BigDecimal.ZERO;
  @Column(name="cost_center") private String costCenter;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected JournalLine() {}
  public JournalLine(UUID facilityId, String governorate, UUID journalId, String accountCode, java.math.BigDecimal debit, java.math.BigDecimal credit, String costCenter) {
    this.facilityId=facilityId; this.governorate=governorate; this.journalId=journalId; this.accountCode=accountCode;
    this.debit=debit==null?java.math.BigDecimal.ZERO:debit; this.credit=credit==null?java.math.BigDecimal.ZERO:credit; this.costCenter=costCenter;
  }
  public UUID getId(){return id;} public java.math.BigDecimal getDebit(){return debit;} public java.math.BigDecimal getCredit(){return credit;}
  public String getAccountCode(){return accountCode;}
}
