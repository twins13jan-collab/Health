package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "patient_complaints")
public class PatientComplaint {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complainant_name") private String complainantName;
  @Column(nullable=false) private boolean anonymous = false;
  private String representation; private String contact;
  @Column(name="identity_verified", nullable=false) private boolean identityVerified = false;
  @Column(nullable=false) private String content;
  private String category;
  @Column(name="imminent_risk", nullable=false) private boolean imminentRisk = false;
  @Column(name="safety_escalated", nullable=false) private boolean safetyEscalated = false;
  @Column(name="safety_case_ref") private String safetyCaseRef;
  @Column(name="root_cause") private String rootCause;
  @Column(name="corrective_action") private String correctiveAction;
  @Column(name="closed_by") private String closedBy;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PatientComplaint() {}
  public PatientComplaint(UUID facilityId, String governorate, String complainantName, boolean anonymous, String representation,
                          String contact, boolean identityVerified, String content, String category, boolean imminentRisk) {
    this.facilityId=facilityId; this.governorate=governorate; this.complainantName=complainantName; this.anonymous=anonymous;
    this.representation=representation; this.contact=contact; this.identityVerified=identityVerified; this.content=content;
    this.category=category; this.imminentRisk=imminentRisk;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public String getContent(){return content;} public boolean isImminentRisk(){return imminentRisk;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void escalateSafety(String ref){ this.safetyEscalated=true; this.safetyCaseRef=ref; this.updatedAt=OffsetDateTime.now(); }
  public void closeWith(String rootCause, String correctiveAction, String by){
    this.rootCause=rootCause; this.correctiveAction=correctiveAction; this.closedBy=by; this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
