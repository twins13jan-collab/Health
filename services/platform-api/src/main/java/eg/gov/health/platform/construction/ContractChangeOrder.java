package eg.gov.health.platform.construction;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "contract_change_orders")
public class ContractChangeOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="project_id", nullable=false) private UUID projectId;
  private String description;
  @Column(name="cost_delta") private BigDecimal costDelta;
  @Column(name="approved_by") private String approvedBy;
  @Column(nullable=false) private boolean approved = false;
  @Column(nullable=false) private boolean applied = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ContractChangeOrder() {}
  public ContractChangeOrder(UUID facilityId, String governorate, UUID projectId, String description, BigDecimal costDelta, String approvedBy, boolean approved) {
    this.facilityId=facilityId; this.governorate=governorate; this.projectId=projectId; this.description=description; this.costDelta=costDelta;
    this.approvedBy=approvedBy; this.approved=approved;
  }
  public UUID getId(){return id;} public boolean isApproved(){return approved;}
  public void apply(){ this.applied=true; }
}
