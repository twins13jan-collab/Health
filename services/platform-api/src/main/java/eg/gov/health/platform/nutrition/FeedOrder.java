package eg.gov.health.platform.nutrition;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "feed_orders")
public class FeedOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(nullable=false) private String kind;
  private String formula; private String components;
  @Column(name="route_detail") private String routeDetail;
  @Column(name="rate_ml_hr") private Double rateMlHr;
  @Column(name="pharmacist_reviewed", nullable=false) private boolean pharmacistReviewed = false;
  @Column(name="reviewed_by") private String reviewedBy;
  @Column(nullable=false) private String status = "ordered";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FeedOrder() {}
  public FeedOrder(UUID facilityId, String governorate, UUID planId, String kind, String formula, String components,
                   String routeDetail, Double rateMlHr) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.kind=kind; this.formula=formula;
    this.components=components; this.routeDetail=routeDetail; this.rateMlHr=rateMlHr;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getKind(){return kind;}
  public String getStatus(){return status;} public boolean isPharmacistReviewed(){return pharmacistReviewed;} public String getComponents(){return components;}
  public void review(String by){ this.pharmacistReviewed=true; this.reviewedBy=by; this.updatedAt=OffsetDateTime.now(); }
  public void start(){ this.status="started"; this.updatedAt=OffsetDateTime.now(); }
  public void hold(){ this.status="held"; this.updatedAt=OffsetDateTime.now(); }
  public void substitute(String components){ this.components=components; this.updatedAt=OffsetDateTime.now(); }
}
