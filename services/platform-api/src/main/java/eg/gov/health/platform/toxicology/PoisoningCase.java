package eg.gov.health.platform.toxicology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "poisoning_cases")
public class PoisoningCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(name="exposure_event_id") private UUID exposureEventId;
  @Column(name="substance_name") private String substanceName;
  @Column(name="substance_unknown", nullable=false) private boolean substanceUnknown = false;
  @Column(name="container_description") private String containerDescription;
  @Column(name="estimated_amount") private String estimatedAmount;
  @Column(name="amount_uncertain", nullable=false) private boolean amountUncertain = false;
  private String route;
  @Column(name="exposure_time") private OffsetDateTime exposureTime;
  @Column(name="age_years") private Integer ageYears;
  @Column(name="weight_kg") private Double weightKg;
  private String symptoms; private String severity;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PoisoningCase() {}
  public PoisoningCase(UUID facilityId, String governorate, UUID patientId, UUID exposureEventId, String substanceName,
                       boolean substanceUnknown, String containerDescription, String estimatedAmount, boolean amountUncertain,
                       String route, OffsetDateTime exposureTime, Integer ageYears, Double weightKg, String symptoms, String severity) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.exposureEventId=exposureEventId;
    this.substanceName=substanceName; this.substanceUnknown=substanceUnknown; this.containerDescription=containerDescription;
    this.estimatedAmount=estimatedAmount; this.amountUncertain=amountUncertain; this.route=route; this.exposureTime=exposureTime;
    this.ageYears=ageYears; this.weightKg=weightKg; this.symptoms=symptoms; this.severity=severity;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public String getSubstanceName(){return substanceName;} public String getRoute(){return route;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void linkEvent(UUID eventId){ this.exposureEventId=eventId; this.updatedAt=OffsetDateTime.now(); }
}
