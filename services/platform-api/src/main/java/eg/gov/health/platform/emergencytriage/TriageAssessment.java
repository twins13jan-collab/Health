package eg.gov.health.platform.emergencytriage;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "triage_assessments")
public class TriageAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="esi_level") private Integer esiLevel;
  @Column(name="arrived_at") private OffsetDateTime arrivedAt;
  @Column(name="doctor_seen_at") private OffsetDateTime doctorSeenAt;
  @Column(nullable=false) private String status = "waiting";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TriageAssessment() {}
  public TriageAssessment(UUID facilityId, String governorate, String patientRef, Integer esiLevel, OffsetDateTime arrivedAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.esiLevel=esiLevel; this.arrivedAt=arrivedAt;
  }
  public UUID getId(){return id;} public Integer getEsiLevel(){return esiLevel;} public OffsetDateTime getArrivedAt(){return arrivedAt;}
  public OffsetDateTime getDoctorSeenAt(){return doctorSeenAt;}
  public void doctorSeen(OffsetDateTime at){ this.doctorSeenAt=at; this.status="in_care"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
