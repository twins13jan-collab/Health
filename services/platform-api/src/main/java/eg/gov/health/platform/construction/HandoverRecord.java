package eg.gov.health.platform.construction;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "handover_records")
public class HandoverRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="project_id", nullable=false) private UUID projectId;
  @Column(name="assets_received", nullable=false) private boolean assetsReceived = false;
  @Column(name="documents_received", nullable=false) private boolean documentsReceived = false;
  @Column(name="licenses_received", nullable=false) private boolean licensesReceived = false;
  @Column(name="accepted_by") private String acceptedBy;
  @Column(name="accepted_at") private OffsetDateTime acceptedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HandoverRecord() {}
  public HandoverRecord(UUID facilityId, String governorate, UUID projectId, boolean assetsReceived, boolean documentsReceived, boolean licensesReceived, String acceptedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.projectId=projectId; this.assetsReceived=assetsReceived;
    this.documentsReceived=documentsReceived; this.licensesReceived=licensesReceived; this.acceptedBy=acceptedBy; this.acceptedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
