package eg.gov.health.platform.referralsdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "waitlist_entries")
public class WaitlistEntry {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_ref") private String serviceRef;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="priority_score") private Integer priorityScore;
  @Column(name="waiting_since") private LocalDate waitingSince;
  @Column(name="fairness_rule_ref") private String fairnessRuleRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected WaitlistEntry() {}
  public WaitlistEntry(UUID facilityId, String governorate, String serviceRef, String patientRef, Integer priorityScore,
                         LocalDate waitingSince, String fairnessRuleRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceRef=serviceRef; this.patientRef=patientRef;
    this.priorityScore=priorityScore; this.waitingSince=waitingSince; this.fairnessRuleRef=fairnessRuleRef;
  }
  public UUID getId(){return id;} public String getPatientRef(){return patientRef;} public Integer getPriorityScore(){return priorityScore;}
  public LocalDate getWaitingSince(){return waitingSince;}
}
