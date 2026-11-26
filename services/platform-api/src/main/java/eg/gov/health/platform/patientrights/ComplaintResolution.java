package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "complaint_resolutions")
public class ComplaintResolution {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complaint_id", nullable=false) private UUID complaintId;
  @Column(nullable=false) private String decision;
  private String basis; @Column(name="decided_by") private String decidedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ComplaintResolution() {}
  public ComplaintResolution(UUID facilityId, String governorate, UUID complaintId, String decision, String basis, String decidedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.complaintId=complaintId; this.decision=decision; this.basis=basis; this.decidedBy=decidedBy;
  }
  public UUID getId(){return id;} public String getDecision(){return decision;}
}
