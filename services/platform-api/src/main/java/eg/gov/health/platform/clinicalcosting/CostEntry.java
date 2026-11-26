package eg.gov.health.platform.clinicalcosting;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cost_entries")
public class CostEntry {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="cost_type") private String costType;
  private String category; private Double amount;
  @Column(name="actually_consumed", nullable=false) private boolean actuallyConsumed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CostEntry() {}
  public CostEntry(UUID facilityId, String governorate, UUID caseId, String costType, String category, Double amount, boolean actuallyConsumed) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.costType=costType; this.category=category; this.amount=amount;
    this.actuallyConsumed=actuallyConsumed;
  }
  public UUID getId(){return id;} public Double getAmount(){return amount;} public boolean isActuallyConsumed(){return actuallyConsumed;}
}
