package eg.gov.health.platform.opendata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "open_datasets")
public class OpenDataset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="min_cell_size") private Integer minCellSize;
  @Column(name="reidentification_risk_assessed", nullable=false) private boolean reidentificationRiskAssessed = false;
  @Column(nullable=false) private boolean anonymized = false;
  @Column(name="definitions_documented", nullable=false) private boolean definitionsDocumented = false;
  @Column(nullable=false) private String status = "draft";
  @Column(name="withdrawn_reason") private String withdrawnReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected OpenDataset() {}
  public OpenDataset(UUID facilityId, String governorate, String name, Integer minCellSize, boolean reidentificationRiskAssessed,
                       boolean anonymized, boolean definitionsDocumented) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.minCellSize=minCellSize;
    this.reidentificationRiskAssessed=reidentificationRiskAssessed; this.anonymized=anonymized; this.definitionsDocumented=definitionsDocumented;
  }
  public UUID getId(){return id;} public boolean isReidentificationRiskAssessed(){return reidentificationRiskAssessed;}
  public boolean isAnonymized(){return anonymized;} public Integer getMinCellSize(){return minCellSize;}
  public boolean isDefinitionsDocumented(){return definitionsDocumented;} public String getWithdrawnReason(){return withdrawnReason;}
  public void publish(){ this.status="published"; this.updatedAt=OffsetDateTime.now(); }
  public void withdraw(String reason){ this.status="withdrawn"; this.withdrawnReason=reason; this.updatedAt=OffsetDateTime.now(); }
}
