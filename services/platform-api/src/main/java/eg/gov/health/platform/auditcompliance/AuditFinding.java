package eg.gov.health.platform.auditcompliance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "audit_findings")
public class AuditFinding {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="audit_ref") private String auditRef;
  private String severity;
  @Column(name="evidence_ref") private String evidenceRef;
  @Column(name="corrective_action") private String correctiveAction;
  @Column(name="verified_closed", nullable=false) private boolean verifiedClosed = false;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AuditFinding() {}
  public AuditFinding(UUID facilityId, String governorate, String auditRef, String severity, String evidenceRef,
                        String correctiveAction, boolean verifiedClosed) {
    this.facilityId=facilityId; this.governorate=governorate; this.auditRef=auditRef; this.severity=severity;
    this.evidenceRef=evidenceRef; this.correctiveAction=correctiveAction; this.verifiedClosed=verifiedClosed;
  }
  public UUID getId(){return id;} public String getEvidenceRef(){return evidenceRef;} public String getCorrectiveAction(){return correctiveAction;}
  public boolean isVerifiedClosed(){return verifiedClosed;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
