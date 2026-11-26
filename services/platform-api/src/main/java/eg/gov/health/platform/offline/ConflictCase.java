package eg.gov.health.platform.offline;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "conflict_cases")
public class ConflictCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="transaction_id", nullable=false) private UUID transactionId;
  @Column(nullable=false) private String domain;
  @Column(name="local_summary") private String localSummary;
  @Column(name="server_summary") private String serverSummary;
  @Column(name="resolution_rule") private String resolutionRule;
  @Column(name="resolved_by") private String resolvedBy;
  private String outcome;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ConflictCase() {}
  public ConflictCase(UUID facilityId, String governorate, UUID transactionId, String domain, String localSummary, String serverSummary) {
    this.facilityId=facilityId; this.governorate=governorate; this.transactionId=transactionId; this.domain=domain;
    this.localSummary=localSummary; this.serverSummary=serverSummary;
  }
  public UUID getId(){return id;} public UUID getTransactionId(){return transactionId;} public String getStatus(){return status;} public String getDomain(){return domain;}
  public void resolve(String rule, String by, String outcome){ this.resolutionRule=rule; this.resolvedBy=by; this.outcome=outcome; this.status="resolved"; this.updatedAt=OffsetDateTime.now(); }
}
