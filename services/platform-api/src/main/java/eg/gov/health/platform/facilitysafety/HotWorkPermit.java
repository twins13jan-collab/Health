package eg.gov.health.platform.facilitysafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "hot_work_permits")
public class HotWorkPermit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="work_type", nullable=false) private String workType;
  private String location;
  @Column(name="requested_by") private String requestedBy;
  @Column(name="approved_by") private String approvedBy;
  @Column(name="fire_watch", nullable=false) private boolean fireWatch = false;
  @Column(name="loto_applied", nullable=false) private boolean lotoApplied = false;
  @Column(name="valid_from") private OffsetDateTime validFrom;
  @Column(name="valid_until") private OffsetDateTime validUntil;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HotWorkPermit() {}
  public HotWorkPermit(UUID facilityId, String governorate, String workType, String location, String requestedBy, boolean fireWatch, boolean lotoApplied, OffsetDateTime validFrom, OffsetDateTime validUntil) {
    this.facilityId=facilityId; this.governorate=governorate; this.workType=workType; this.location=location; this.requestedBy=requestedBy;
    this.fireWatch=fireWatch; this.lotoApplied=lotoApplied; this.validFrom=validFrom; this.validUntil=validUntil;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getWorkType(){return workType;}
  public String getRequestedBy(){return requestedBy;} public boolean isFireWatch(){return fireWatch;} public boolean isLotoApplied(){return lotoApplied;}
  public OffsetDateTime getValidFrom(){return validFrom;} public OffsetDateTime getValidUntil(){return validUntil;}
  public void approve(String by){ this.approvedBy=by; this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
}
