package eg.gov.health.platform.supplychaindeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "supply_risk_assessments")
public class SupplyRiskAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="item_ref") private String itemRef;
  @Column(name="alternative_identified", nullable=false) private boolean alternativeIdentified = false;
  @Column(nullable=false) private boolean managed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SupplyRiskAssessment() {}
  public SupplyRiskAssessment(UUID facilityId, String governorate, String itemRef, boolean alternativeIdentified) {
    this.facilityId=facilityId; this.governorate=governorate; this.itemRef=itemRef; this.alternativeIdentified=alternativeIdentified;
  }
  public UUID getId(){return id;}
  public void markManaged(){ this.managed=true; this.updatedAt=OffsetDateTime.now(); }
}
