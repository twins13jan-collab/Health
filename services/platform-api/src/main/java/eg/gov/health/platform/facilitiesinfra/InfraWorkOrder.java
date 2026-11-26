package eg.gov.health.platform.facilitiesinfra;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "infra_work_orders")
public class InfraWorkOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="system_id", nullable=false) private UUID systemId;
  private String criticality; private String description;
  @Column(name="fallback_plan") private String fallbackPlan;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InfraWorkOrder() {}
  public InfraWorkOrder(UUID facilityId, String governorate, UUID systemId, String criticality, String description, String fallbackPlan) {
    this.facilityId=facilityId; this.governorate=governorate; this.systemId=systemId; this.criticality=criticality; this.description=description;
    this.fallbackPlan=fallbackPlan;
  }
  public UUID getId(){return id;} public UUID getSystemId(){return systemId;} public String getCriticality(){return criticality;} public String getFallbackPlan(){return fallbackPlan;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
