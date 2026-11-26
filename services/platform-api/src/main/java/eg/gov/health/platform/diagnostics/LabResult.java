package eg.gov.health.platform.diagnostics;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "lab_results")
public class LabResult {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  @Column(nullable=false) private BigDecimal value;
  private String unit;
  @Column(name="ref_low") private BigDecimal refLow;
  @Column(name="ref_high") private BigDecimal refHigh;
  @Column(name="is_critical", nullable=false) private boolean critical;
  @Column(nullable=false) private int version = 1;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(nullable=false) private String status = "resulted"; // resulted, verified, corrected
  @Column(name="verified_by") private String verifiedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LabResult() {}
  public LabResult(UUID facilityId, String governorate, UUID orderId, BigDecimal value, String unit, BigDecimal refLow, BigDecimal refHigh, boolean critical, UUID supersedesId) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.value=value; this.unit=unit;
    this.refLow=refLow; this.refHigh=refHigh; this.critical=critical; this.supersedesId=supersedesId;
    if (supersedesId != null) this.status = "corrected";
  }
  public UUID getId(){return id;} public UUID getOrderId(){return orderId;} public BigDecimal getValue(){return value;}
  public boolean isCritical(){return critical;} public String getStatus(){return status;} public int getVersion(){return version;}
  public void setVersion(int v){this.version=v;} public void verify(String by){this.status="verified"; this.verifiedBy=by; this.updatedAt=OffsetDateTime.now();}
  public UUID getFacilityId(){return facilityId;} public String getUnit()
  {return unit;}

  public java.math.BigDecimal getRefLow(){return refLow;} public java.math.BigDecimal getRefHigh(){return refHigh;}
}
