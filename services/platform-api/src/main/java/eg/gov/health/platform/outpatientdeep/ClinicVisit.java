package eg.gov.health.platform.outpatientdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinic_visits")
public class ClinicVisit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String specialty;
  @Column(name="visit_type") private String visitType;
  @Column(name="identity_verified", nullable=false) private boolean identityVerified = false;
  @Column(name="eligibility_verified", nullable=false) private boolean eligibilityVerified = false;
  @Column(name="plan_documented", nullable=false) private boolean planDocumented = false;
  @Column(name="followup_owner") private String followupOwner;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ClinicVisit() {}
  public ClinicVisit(UUID facilityId, String governorate, String patientRef, String specialty, String visitType,
                       boolean identityVerified, boolean eligibilityVerified, boolean planDocumented, String followupOwner) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.specialty=specialty; this.visitType=visitType;
    this.identityVerified=identityVerified; this.eligibilityVerified=eligibilityVerified; this.planDocumented=planDocumented;
    this.followupOwner=followupOwner;
  }
  public UUID getId(){return id;} public boolean isIdentityVerified(){return identityVerified;} public boolean isEligibilityVerified(){return eligibilityVerified;}
  public boolean isPlanDocumented(){return planDocumented;} public String getFollowupOwner(){return followupOwner;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
  public void markNoShow(){ this.status="no_show"; this.updatedAt=OffsetDateTime.now(); }
}
