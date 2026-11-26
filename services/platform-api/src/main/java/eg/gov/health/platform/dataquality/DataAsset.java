package eg.gov.health.platform.dataquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "data_assets")
public class DataAsset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="owner_ref") private String ownerRef;
  @Column(name="source_of_truth") private String sourceOfTruth;
  @Column(name="completeness_pct") private Double completenessPct;
  @Column(name="validity_pct") private Double validityPct;
  @Column(name="consistency_pct") private Double consistencyPct;
  @Column(name="freshness_hours") private Double freshnessHours;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DataAsset() {}
  public DataAsset(UUID facilityId, String governorate, String name, String ownerRef, String sourceOfTruth,
                   Double completenessPct, Double validityPct, Double consistencyPct, Double freshnessHours) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.ownerRef=ownerRef; this.sourceOfTruth=sourceOfTruth;
    this.completenessPct=completenessPct; this.validityPct=validityPct; this.consistencyPct=consistencyPct; this.freshnessHours=freshnessHours;
  }
  public UUID getId(){return id;} public Double getCompletenessPct(){return completenessPct;} public Double getValidityPct(){return validityPct;}
  public Double getConsistencyPct(){return consistencyPct;} public Double getFreshnessHours(){return freshnessHours;}
}
