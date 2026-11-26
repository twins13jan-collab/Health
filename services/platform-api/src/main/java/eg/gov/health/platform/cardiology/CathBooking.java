package eg.gov.health.platform.cardiology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cath_bookings")
public class CathBooking {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="pathway_id") private UUID pathwayId;
  @Column(name="patient_ref") private String patientRef;
  private String lab; @Column(name="scheduled_at") private OffsetDateTime scheduledAt;
  @Column(name="consent_confirmed", nullable=false) private boolean consentConfirmed = false;
  @Column(name="allergy_checked", nullable=false) private boolean allergyChecked = false;
  @Column(name="renal_function") private String renalFunction;
  @Column(name="antiplatelet_loaded", nullable=false) private boolean antiplateletLoaded = false;
  @Column(name="team_confirmed", nullable=false) private boolean teamConfirmed = false;
  @Column(nullable=false) private String status = "booked";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CathBooking() {}
  public CathBooking(UUID facilityId, String governorate, UUID pathwayId, String patientRef, String lab, OffsetDateTime scheduledAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.pathwayId=pathwayId; this.patientRef=patientRef; this.lab=lab; this.scheduledAt=scheduledAt;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public boolean isConsentConfirmed(){return consentConfirmed;} public boolean isAllergyChecked(){return allergyChecked;}
  public String getRenalFunction(){return renalFunction;} public boolean isAntiplateletLoaded(){return antiplateletLoaded;}
  public boolean isTeamConfirmed(){return teamConfirmed;} public UUID getPathwayId(){return pathwayId;}
  public void prepare(boolean consent, boolean allergy, String renal, boolean antiplatelet, boolean team){
    this.consentConfirmed=consent; this.allergyChecked=allergy; this.renalFunction=renal;
    this.antiplateletLoaded=antiplatelet; this.teamConfirmed=team;
    if (CardiologyRules.preparationComplete(consent, allergy, renal, antiplatelet, team)) this.status="prepared";
    this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
