package eg.gov.health.platform.stroke;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "stroke_activations")
public class StrokeActivation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="last_known_well_at", nullable=false) private OffsetDateTime lastKnownWellAt;
  @Column(name="arrival_at", nullable=false) private OffsetDateTime arrivalAt;
  private Integer nihss; @Column(name="ct_at") private OffsetDateTime ctAt;
  @Column(name="ct_result") private String ctResult;
  @Column(name="hemorrhage_excluded", nullable=false) private boolean hemorrhageExcluded = false;
  @Column(nullable=false) private String status = "activated";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StrokeActivation() {}
  public StrokeActivation(UUID facilityId, String governorate, String patientRef, OffsetDateTime lastKnownWellAt, OffsetDateTime arrivalAt, Integer nihss) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef;
    this.lastKnownWellAt=lastKnownWellAt; this.arrivalAt=arrivalAt; this.nihss=nihss;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;}
  public OffsetDateTime getLastKnownWellAt(){return lastKnownWellAt;} public OffsetDateTime getArrivalAt(){return arrivalAt;}
  public boolean isHemorrhageExcluded(){return hemorrhageExcluded;} public String getStatus(){return status;}
  public void recordCt(OffsetDateTime at, String result, boolean hemorrhageExcluded){
    this.ctAt=at; this.ctResult=result; this.hemorrhageExcluded=hemorrhageExcluded; this.status="imaged"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
