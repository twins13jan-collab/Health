package eg.gov.health.platform.construction;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "design_approvals")
public class DesignApproval {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="project_id", nullable=false) private UUID projectId;
  private String discipline;
  @Column(name="approved_by") private String approvedBy;
  @Column(nullable=false) private boolean approved = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DesignApproval() {}
  public DesignApproval(UUID facilityId, String governorate, UUID projectId, String discipline, String approvedBy, boolean approved) {
    this.facilityId=facilityId; this.governorate=governorate; this.projectId=projectId; this.discipline=discipline; this.approvedBy=approvedBy; this.approved=approved;
  }
  public UUID getId(){return id;}
}
