package eg.gov.health.platform.proms;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "prom_schedules")
public class PromSchedule {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="instrument_id", nullable=false) private UUID instrumentId;
  @Column(name="care_pathway") private String carePathway;
  @Column(name="timing_point") private String timingPoint;
  @Column(name="due_at") private LocalDate dueAt;
  @Column(nullable=false) private String status = "scheduled";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PromSchedule() {}
  public PromSchedule(UUID facilityId, String governorate, String patientRef, UUID instrumentId, String carePathway, String timingPoint, LocalDate dueAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.instrumentId=instrumentId;
    this.carePathway=carePathway; this.timingPoint=timingPoint; this.dueAt=dueAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
