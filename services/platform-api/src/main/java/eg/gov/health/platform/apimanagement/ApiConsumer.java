package eg.gov.health.platform.apimanagement;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "api_consumers")
public class ApiConsumer {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="product_id", nullable=false) private UUID productId;
  @Column(name="consumer_ref") private String consumerRef;
  @Column(name="quota_per_day") private Integer quotaPerDay;
  @Column(name="usage_today", nullable=false) private int usageToday = 0;
  @Column(name="key_status", nullable=false) private String keyStatus = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ApiConsumer() {}
  public ApiConsumer(UUID facilityId, String governorate, UUID productId, String consumerRef, Integer quotaPerDay) {
    this.facilityId=facilityId; this.governorate=governorate; this.productId=productId; this.consumerRef=consumerRef; this.quotaPerDay=quotaPerDay;
  }
  public UUID getId(){return id;} public String getKeyStatus(){return keyStatus;} public Integer getUsageToday(){return usageToday;} public Integer getQuotaPerDay(){return quotaPerDay;}
  public void revokeKey(){ this.keyStatus="revoked"; this.updatedAt=OffsetDateTime.now(); }
  public void recordUsage(int n){ this.usageToday+=n; this.updatedAt=OffsetDateTime.now(); }
}
