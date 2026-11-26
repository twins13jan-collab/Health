package eg.gov.health.platform.hazmat;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "exposure_records")
public class ExposureRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(name="person_ref") private String personRef;
  @Column(name="exposure_level") private String exposureLevel;
  @Column(nullable=false) private boolean contaminated = false;
  @Column(nullable=false) private boolean decontaminated = false;
  @Column(name="in_treatment", nullable=false) private boolean inTreatment = false;
  @Column(name="follow_up_required", nullable=false) private boolean followUpRequired = false;
  @Column(name="follow_up_done", nullable=false) private boolean followUpDone = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ExposureRecord() {}
  public ExposureRecord(UUID facilityId, String governorate, UUID incidentId, String personRef, String exposureLevel,
                        boolean contaminated, boolean followUpRequired) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.personRef=personRef; this.exposureLevel=exposureLevel;
    this.contaminated=contaminated; this.followUpRequired=followUpRequired;
  }
  public UUID getId(){return id;} public boolean isContaminated(){return contaminated;} public boolean isDecontaminated(){return decontaminated;}
  public void decontaminate(){ this.decontaminated=true; this.updatedAt=OffsetDateTime.now(); }
  public void enterTreatment(){ this.inTreatment=true; this.updatedAt=OffsetDateTime.now(); }
  public void completeFollowUp(){ this.followUpDone=true; this.updatedAt=OffsetDateTime.now(); }
}
