package eg.gov.health.platform.hrdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "training_records")
public class TrainingRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="staff_ref") private String staffRef;
  private String course;
  @Column(nullable=false) private boolean mandatory = false;
  @Column(nullable=false) private boolean completed = false;
  @Column(name="due_date") private LocalDate dueDate;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TrainingRecord() {}
  public TrainingRecord(UUID facilityId, String governorate, String staffRef, String course, boolean mandatory, boolean completed, LocalDate dueDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.staffRef=staffRef; this.course=course; this.mandatory=mandatory;
    this.completed=completed; this.dueDate=dueDate;
  }
  public UUID getId(){return id;}
  public String getStaffRef(){return staffRef;} public String getCourse(){return course;}
  public boolean isMandatory(){return mandatory;} public boolean isCompleted(){return completed;}
  public LocalDate getDueDate(){return dueDate;} public UUID getFacilityId(){return facilityId;}
}
