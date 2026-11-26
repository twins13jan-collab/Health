package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "accounting_periods")
public class AccountingPeriod {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="period_key", nullable=false) private String periodKey;
  @Column(nullable=false) private String status = "open";
  @Column(name="closed_by") private String closedBy;
  @Column(name="reopen_reason") private String reopenReason;
  @Column(name="reopened_by") private String reopenedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AccountingPeriod() {}
  public AccountingPeriod(UUID facilityId, String governorate, String periodKey) {
    this.facilityId=facilityId; this.governorate=governorate; this.periodKey=periodKey;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getPeriodKey(){return periodKey;}
  public UUID getFacilityId(){return facilityId;}
  public void close(String by){ this.status="closed"; this.closedBy=by; this.updatedAt=OffsetDateTime.now(); }
  public void reopen(String by, String reason){ this.status="reopened"; this.reopenedBy=by; this.reopenReason=reason; this.updatedAt=OffsetDateTime.now(); }
}
