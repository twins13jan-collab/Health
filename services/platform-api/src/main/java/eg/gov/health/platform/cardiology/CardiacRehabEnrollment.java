package eg.gov.health.platform.cardiology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cardiac_rehab_enrollments")
public class CardiacRehabEnrollment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String program;
  @Column(nullable=false) private boolean statin = false;
  @Column(nullable=false) private boolean antiplatelet = false;
  @Column(name="beta_blocker", nullable=false) private boolean betaBlocker = false;
  @Column(name="smoking_cessation", nullable=false) private boolean smokingCessation = false;
  @Column(nullable=false) private String status = "enrolled";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CardiacRehabEnrollment() {}
  public CardiacRehabEnrollment(UUID facilityId, String governorate, String patientRef, String program, boolean statin, boolean antiplatelet, boolean betaBlocker, boolean smokingCessation) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.program=program;
    this.statin=statin; this.antiplatelet=antiplatelet; this.betaBlocker=betaBlocker; this.smokingCessation=smokingCessation;
  }
  public UUID getId(){return id;}
}
