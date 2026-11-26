package eg.gov.health.platform.tempworkforce;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "temp_assignments")
public class TempAssignment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="provider_id", nullable=false) private UUID providerId;
  private String department;
  @Column(name="scope_of_practice") private String scopeOfPractice;
  @Column(name="shift_start") private OffsetDateTime shiftStart;
  @Column(name="shift_end") private OffsetDateTime shiftEnd;
  @Column(nullable=false) private String status = "offered";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TempAssignment() {}
  public TempAssignment(UUID facilityId, String governorate, UUID providerId, String department, String scopeOfPractice, OffsetDateTime shiftStart, OffsetDateTime shiftEnd) {
    this.facilityId=facilityId; this.governorate=governorate; this.providerId=providerId; this.department=department;
    this.scopeOfPractice=scopeOfPractice; this.shiftStart=shiftStart; this.shiftEnd=shiftEnd;
  }
  public UUID getId(){return id;} public OffsetDateTime getShiftStart(){return shiftStart;} public OffsetDateTime getShiftEnd(){return shiftEnd;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
