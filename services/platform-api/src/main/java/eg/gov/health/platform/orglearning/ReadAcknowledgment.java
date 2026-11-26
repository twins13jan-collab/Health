package eg.gov.health.platform.orglearning;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "read_acknowledgments")
public class ReadAcknowledgment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="document_id", nullable=false) private UUID documentId;
  @Column(name="user_ref") private String userRef;
  @Column(nullable=false) private boolean acknowledged = false;
  @Column(name="comprehension_assessed", nullable=false) private boolean comprehensionAssessed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ReadAcknowledgment() {}
  public ReadAcknowledgment(UUID facilityId, String governorate, UUID documentId, String userRef, boolean acknowledged, boolean comprehensionAssessed) {
    this.facilityId=facilityId; this.governorate=governorate; this.documentId=documentId; this.userRef=userRef;
    this.acknowledged=acknowledged; this.comprehensionAssessed=comprehensionAssessed;
  }
  public UUID getId(){return id;} public boolean isAcknowledged(){return acknowledged;} public boolean isComprehensionAssessed(){return comprehensionAssessed;}
}
