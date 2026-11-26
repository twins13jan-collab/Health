package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "bank_transactions")
public class BankTransaction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="statement_id", nullable=false) private UUID statementId;
  private String reference; @Column(name="tx_date") private LocalDate txDate;
  @Column(nullable=false) private java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
  @Column(name="matched_journal_id") private UUID matchedJournalId;
  @Column(name="match_status", nullable=false) private String matchStatus = "unmatched";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected BankTransaction() {}
  public BankTransaction(UUID facilityId, String governorate, UUID statementId, String reference, LocalDate txDate, java.math.BigDecimal amount) {
    this.facilityId=facilityId; this.governorate=governorate; this.statementId=statementId; this.reference=reference;
    this.txDate=txDate; this.amount=amount==null?java.math.BigDecimal.ZERO:amount;
  }
  public UUID getId(){return id;} public String getReference(){return reference;} public java.math.BigDecimal getAmount(){return amount;}
  public String getMatchStatus(){return matchStatus;}
  public void match(UUID journalId){ this.matchedJournalId=journalId; this.matchStatus="matched"; }
}
