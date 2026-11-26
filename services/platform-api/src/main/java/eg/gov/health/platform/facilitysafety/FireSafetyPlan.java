package eg.gov.health.platform.facilitysafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "fire_safety_plans")
public class FireSafetyPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String building; @Column(name="evacuation_routes") private String evacuationRoutes;
  @Column(name="assembly_points") private String assemblyPoints;
  @Column(name="approved_by") private String approvedBy;
  @Column(name="review_due") private LocalDate reviewDue;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FireSafetyPlan() {}
  public FireSafetyPlan(UUID facilityId, String governorate, String building, String evacuationRoutes, String assemblyPoints, String approvedBy, LocalDate reviewDue) {
    this.facilityId=facilityId; this.governorate=governorate; this.building=building; this.evacuationRoutes=evacuationRoutes;
    this.assemblyPoints=assemblyPoints; this.approvedBy=approvedBy; this.reviewDue=reviewDue;
  }
  public UUID getId(){return id;}
}
