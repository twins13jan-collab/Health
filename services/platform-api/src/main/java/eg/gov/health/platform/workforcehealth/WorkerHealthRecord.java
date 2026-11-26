package eg.gov.health.platform.workforcehealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "worker_health_records")
public class WorkerHealthRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_ref") private String workerRef;
  @Column(name="worker_role") private String workerRole;
  @Column(name="last_periodic_exam_date") private LocalDate lastPeriodicExamDate;
  @Column(name="vaccinations_up_to_date", nullable=false) private boolean vaccinationsUpToDate = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WorkerHealthRecord() {}
  public WorkerHealthRecord(UUID facilityId, String governorate, String workerRef, String workerRole, LocalDate lastPeriodicExamDate, boolean vaccinationsUpToDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerRef=workerRef; this.workerRole=workerRole;
    this.lastPeriodicExamDate=lastPeriodicExamDate; this.vaccinationsUpToDate=vaccinationsUpToDate;
  }
  public UUID getId(){return id;} public LocalDate getLastPeriodicExamDate(){return lastPeriodicExamDate;} public boolean isVaccinationsUpToDate(){return vaccinationsUpToDate;}
}
