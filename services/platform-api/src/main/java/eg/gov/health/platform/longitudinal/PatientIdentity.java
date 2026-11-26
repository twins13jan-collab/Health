package eg.gov.health.platform.longitudinal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patient_identities")
public class PatientIdentity {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(name="identity_kind", nullable=false) private String identityKind = "temporary";
  @Column(name="proof_source") private String proofSource;
  @Column(name="verification_status", nullable=false) private String verificationStatus = "pending";
  @Column(name="merged_into") private UUID mergedInto;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PatientIdentity() {}
  public PatientIdentity(UUID facilityId, String governorate, UUID patientId, String identityKind, String proofSource) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId;
    this.identityKind=identityKind==null?"temporary":identityKind; this.proofSource=proofSource;
    this.verificationStatus = "verified".equals(this.identityKind) ? "verified" : "pending";
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public String getIdentityKind(){return identityKind;} public String getStatus(){return status;} public UUID getMergedInto(){return mergedInto;}
  public void verify(String proof){ this.identityKind="verified"; this.verificationStatus="verified"; this.proofSource=proof; this.updatedAt=OffsetDateTime.now(); }
  public void markMerged(UUID into){ this.status="merged"; this.mergedInto=into; this.updatedAt=OffsetDateTime.now(); }
  public void unmerge(){ this.status="active"; this.mergedInto=null; this.updatedAt=OffsetDateTime.now(); }
}
