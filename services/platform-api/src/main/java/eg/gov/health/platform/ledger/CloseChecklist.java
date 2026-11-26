package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "close_checklists")
public class CloseChecklist {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="period_key", nullable=false) private String periodKey;
  @Column(name="pending_entries", nullable=false) private int pendingEntries = 0;
  private String approvals; @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CloseChecklist() {}
  public CloseChecklist(UUID facilityId, String governorate, String periodKey, int pendingEntries, String approvals) {
    this.facilityId=facilityId; this.governorate=governorate; this.periodKey=periodKey; this.pendingEntries=pendingEntries; this.approvals=approvals;
  }
  public UUID getId(){return id;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
