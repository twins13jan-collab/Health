package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "reconciliation_cases")
public class ReconciliationCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="message_ref", nullable=false) private UUID messageRef;
  private String difference; private String handling;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ReconciliationCase() {}
  public ReconciliationCase(UUID facilityId, String governorate, UUID messageRef, String difference, String handling) {
    this.facilityId=facilityId; this.governorate=governorate; this.messageRef=messageRef; this.difference=difference; this.handling=handling;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void resolve(String handling){ this.handling=handling; this.status="resolved"; this.updatedAt=OffsetDateTime.now(); }
}
