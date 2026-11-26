package eg.gov.health.platform.governingrules;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "rule_compliance_checks")
public class RuleComplianceCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="rule_id", nullable=false) private UUID ruleId;
  @Column(name="scope_ref") private String scopeRef;
  @Column(nullable=false) private boolean compliant = false;
  @Column(name="evidence_ref") private String evidenceRef;
  @Column(name="checked_by") private String checkedBy;
  @Column(nullable=false) private boolean independent = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RuleComplianceCheck() {}
  public RuleComplianceCheck(UUID facilityId, String governorate, UUID ruleId, String scopeRef, boolean compliant,
                               String evidenceRef, String checkedBy, boolean independent) {
    this.facilityId=facilityId; this.governorate=governorate; this.ruleId=ruleId; this.scopeRef=scopeRef; this.compliant=compliant;
    this.evidenceRef=evidenceRef; this.checkedBy=checkedBy; this.independent=independent;
  }
  public UUID getId(){return id;} public UUID getRuleId(){return ruleId;} public boolean isCompliant(){return compliant;}
  public String getEvidenceRef(){return evidenceRef;} public boolean isIndependent(){return independent;}
}
