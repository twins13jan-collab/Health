package eg.gov.health.platform.recordsdocs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "document_access_events")
public class DocumentAccessEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="document_id", nullable=false) private UUID documentId;
  private String actor; @Column(nullable=false) private String action;
  private String purpose; @Column(nullable=false) private boolean sensitive = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DocumentAccessEvent() {}
  public DocumentAccessEvent(UUID facilityId, String governorate, UUID documentId, String actor, String action, String purpose, boolean sensitive) {
    this.facilityId=facilityId; this.governorate=governorate; this.documentId=documentId; this.actor=actor; this.action=action; this.purpose=purpose; this.sensitive=sensitive;
  }
  public UUID getId(){return id;}
}
