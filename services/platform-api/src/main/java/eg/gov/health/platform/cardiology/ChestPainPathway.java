package eg.gov.health.platform.cardiology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "chest_pain_pathways")
public class ChestPainPathway {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="arrival_at", nullable=false) private OffsetDateTime arrivalAt;
  @Column(name="ecg_at") private OffsetDateTime ecgAt;
  @Column(name="ecg_finding") private String ecgFinding;
  @Column(name="troponin_result") private String troponinResult;
  @Column(name="troponin_at") private OffsetDateTime troponinAt;
  private String pathway; @Column(name="balloon_at") private OffsetDateTime balloonAt;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ChestPainPathway() {}
  public ChestPainPathway(UUID facilityId, String governorate, String patientRef, OffsetDateTime arrivalAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.arrivalAt=arrivalAt;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public OffsetDateTime getArrivalAt(){return arrivalAt;}
  public String getEcgFinding(){return ecgFinding;} public String getStatus(){return status;}
  public void recordEcg(OffsetDateTime at, String finding){ this.ecgAt=at; this.ecgFinding=finding; this.updatedAt=OffsetDateTime.now(); }
  public void recordTroponin(String result, OffsetDateTime at){ this.troponinResult=result; this.troponinAt=at; this.updatedAt=OffsetDateTime.now(); }
  public void setPathway(String p){ this.pathway=p; this.updatedAt=OffsetDateTime.now(); }
  public void recordBalloon(OffsetDateTime at){ this.balloonAt=at; this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
