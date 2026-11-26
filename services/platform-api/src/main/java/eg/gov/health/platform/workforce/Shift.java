package eg.gov.health.platform.workforce;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "shifts")
public class Shift {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="staff_id", nullable=false) private UUID staffId;
  @Column(name="unit") private String unit;
  @Column(name="starts_at", nullable=false) private OffsetDateTime startsAt;
  @Column(name="ends_at", nullable=false) private OffsetDateTime endsAt;
  @Column(nullable=false) private String kind = "regular"; // regular, on_call, overtime
  @Column(nullable=false) private String status = "scheduled"; // scheduled, completed, swapped, cancelled, absent
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Shift() {}
  public Shift(UUID facilityId, String governorate, UUID staffId, String unit, OffsetDateTime startsAt, OffsetDateTime endsAt, String kind) {
    this.facilityId=facilityId; this.governorate=governorate; this.staffId=staffId; this.unit=unit;
    this.startsAt=startsAt; this.endsAt=endsAt; this.kind=kind==null?"regular":kind;
  }
  public UUID getId(){return id;} public UUID getStaffId(){return staffId;}
  public OffsetDateTime getStartsAt(){return startsAt;} public OffsetDateTime getEndsAt(){return endsAt;}
  public String getStatus(){return status;} public String getUnit(){return unit;} public String getKind(){return kind;}
  public UUID getFacilityId(){return facilityId;}
  public ShiftRules.ShiftView toView(){ return new ShiftRules.ShiftView(startsAt, endsAt, status); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
