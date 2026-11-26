package eg.gov.health.platform.infectioncontrol;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "infection_cases")
public class InfectionCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String organism;
  @Column(name="case_type", nullable=false) private String caseType = "suspected";
  private String source; @Column(name="hai_type") private String haiType;
  private String unit; @Column(name="onset_date") private LocalDate onsetDate;
  @Column(nullable=false) private boolean notifiable = false;
  @Column(name="notified_at") private OffsetDateTime notifiedAt;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InfectionCase() {}
  public InfectionCase(UUID facilityId, String governorate, String patientRef, String organism, String caseType, String source, String haiType, String unit, LocalDate onsetDate, boolean notifiable) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.organism=organism;
    this.caseType=caseType==null?"suspected":caseType; this.source=source; this.haiType=haiType; this.unit=unit;
    this.onsetDate=onsetDate; this.notifiable=notifiable;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public boolean isNotifiable(){return notifiable;}
  public String getCaseType(){return caseType;} public OffsetDateTime getNotifiedAt(){return notifiedAt;} public String getStatus(){return status;}
  public void confirm(){ this.caseType="confirmed"; this.updatedAt=OffsetDateTime.now(); }
  public void notifyNow(){ this.notifiedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
