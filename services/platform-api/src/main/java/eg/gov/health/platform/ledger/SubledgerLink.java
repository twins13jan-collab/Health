package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "subledger_links")
public class SubledgerLink {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="journal_id", nullable=false) private UUID journalId;
  @Column(name="subledger_type", nullable=false) private String subledgerType;
  @Column(name="subledger_ref", nullable=false) private String subledgerRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SubledgerLink() {}
  public SubledgerLink(UUID facilityId, String governorate, UUID journalId, String subledgerType, String subledgerRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.journalId=journalId; this.subledgerType=subledgerType; this.subledgerRef=subledgerRef;
  }
  public UUID getId(){return id;}
}
