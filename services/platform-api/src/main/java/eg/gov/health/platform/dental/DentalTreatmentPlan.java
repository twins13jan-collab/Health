package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dental_treatment_plans")
public class DentalTreatmentPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String diagnosis;
  private String plan;
  @Column(name="consent_documented", nullable=false) private boolean consentDocumented = false;
  @Column(nullable=false) private String status = "proposed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DentalTreatmentPlan() {}
  public DentalTreatmentPlan(UUID facilityId, String governorate, UUID patientId, String diagnosis, String plan, boolean consentDocumented) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.diagnosis=diagnosis;
    this.plan=plan; this.consentDocumented=consentDocumented;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public boolean isConsentDocumented(){return consentDocumented;}
}
