package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "allergies")
public class Allergy {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String allergen;
  @Column(name="allergen_class") private String allergenClass;
  @Column(nullable=false) private String severity = "moderate"; // mild, moderate, severe, life_threatening
  private String reaction;
  @Column(nullable=false) private String status = "active"; // active, resolved, entered_in_error
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Allergy() {}
  public Allergy(UUID facilityId, String governorate, UUID patientId, String allergen, String allergenClass, String severity, String reaction, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.allergen=allergen;
    this.allergenClass=allergenClass; this.severity=severity==null?"moderate":severity; this.reaction=reaction; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;}
  public AllergyRules.AllergyView toView(){ return new AllergyRules.AllergyView(allergen, allergenClass, severity, status); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
