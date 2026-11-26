package eg.gov.health.platform.occhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "occupational_examinations")
public class OccupationalExamination {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_id", nullable=false) private UUID workerId;
  @Column(name="exam_type") private String examType;
  @Column(name="exposure_type") private String exposureType;
  private String result; private String restrictions;
  @Column(name="diagnosis_note") private String diagnosisNote;
  @Column(name="examined_at") private LocalDate examinedAt;
  @Column(name="next_due_at") private LocalDate nextDueAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OccupationalExamination() {}
  public OccupationalExamination(UUID facilityId, String governorate, UUID workerId, String examType, String exposureType, String result,
                                 String restrictions, String diagnosisNote, LocalDate examinedAt, LocalDate nextDueAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerId=workerId; this.examType=examType; this.exposureType=exposureType;
    this.result=result; this.restrictions=restrictions; this.diagnosisNote=diagnosisNote; this.examinedAt=examinedAt; this.nextDueAt=nextDueAt;
  }
  public UUID getId(){return id;} public String getResult(){return result;} public String getRestrictions(){return restrictions;}
  public LocalDate getNextDueAt(){return nextDueAt;} public String getExamType(){return examType;}
}
