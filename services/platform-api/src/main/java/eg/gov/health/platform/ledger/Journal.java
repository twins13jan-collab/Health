package eg.gov.health.platform.ledger;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "journals")
public class Journal {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="source_system", nullable=false) private String sourceSystem;
  @Column(name="source_ref") private String sourceRef;
  @Column(name="period_key", nullable=false) private String periodKey;
  @Column(nullable=false) private String currency = "EGP";
  @Column(name="fx_rate_source") private String fxRateSource;
  @Column(name="fx_rate_date") private LocalDate fxRateDate;
  @Column(name="reversal_of") private UUID reversalOf;
  @Column(name="posted_by") private String postedBy;
  @Column(name="created_by") private String createdBy;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Journal() {}
  public Journal(UUID facilityId, String governorate, String sourceSystem, String sourceRef, String periodKey,
                 String currency, String fxRateSource, LocalDate fxRateDate, UUID reversalOf, String createdBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.sourceSystem=sourceSystem; this.sourceRef=sourceRef;
    this.periodKey=periodKey; this.currency=currency==null?"EGP":currency; this.fxRateSource=fxRateSource;
    this.fxRateDate=fxRateDate; this.reversalOf=reversalOf; this.createdBy=createdBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public String getPeriodKey(){return periodKey;} public String getSourceRef(){return sourceRef;}
  public void post(String by){ this.status="posted"; this.postedBy=by; this.updatedAt=OffsetDateTime.now(); }
  public void markReversed(){ this.status="reversed"; this.updatedAt=OffsetDateTime.now(); }
}
