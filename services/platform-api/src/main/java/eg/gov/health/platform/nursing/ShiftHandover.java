package eg.gov.health.platform.nursing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "shift_handovers")
public class ShiftHandover {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="from_nurse") private String fromNurse;
  @Column(name="to_nurse") private String toNurse;
  @Column(name="critical_cases_note") private String criticalCasesNote;
  @Column(name="open_tasks_count") private Integer openTasksCount;
  @Column(nullable=false) private boolean accepted = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ShiftHandover() {}
  public ShiftHandover(UUID facilityId, String governorate, String fromNurse, String toNurse, String criticalCasesNote, Integer openTasksCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.fromNurse=fromNurse; this.toNurse=toNurse;
    this.criticalCasesNote=criticalCasesNote; this.openTasksCount=openTasksCount;
  }
  public UUID getId(){return id;}
  public void accept(){ this.accepted=true; this.updatedAt=OffsetDateTime.now(); }
}
