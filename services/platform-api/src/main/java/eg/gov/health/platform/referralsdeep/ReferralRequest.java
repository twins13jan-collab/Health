package eg.gov.health.platform.referralsdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "referral_requests")
public class ReferralRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="clinical_reason") private String clinicalReason;
  @Column(name="attachments_complete", nullable=false) private boolean attachmentsComplete = false;
  private String priority;
  @Column(nullable=false) private String status = "pending";
  @Column(name="rejection_reason") private String rejectionReason;
  @Column(name="alternative_offered") private String alternativeOffered;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ReferralRequest() {}
  public ReferralRequest(UUID facilityId, String governorate, String patientRef, String clinicalReason, boolean attachmentsComplete, String priority) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.clinicalReason=clinicalReason;
    this.attachmentsComplete=attachmentsComplete; this.priority=priority;
  }
  public UUID getId(){return id;}
  public void accept(){ this.status="accepted"; this.updatedAt=OffsetDateTime.now(); }
  public void reject(String reason, String alternative){ this.status="rejected"; this.rejectionReason=reason; this.alternativeOffered=alternative; this.updatedAt=OffsetDateTime.now(); }
}
