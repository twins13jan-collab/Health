package eg.gov.health.platform.legalcompliance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "legal_requirements")
public class LegalRequirement {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title;
  @Column(name="source_type") private String sourceType;
  @Column(name="effective_from") private LocalDate effectiveFrom;
  @Column(name="effective_to") private LocalDate effectiveTo;
  @Column(name="policy_ref") private String policyRef;
  @Column(name="procedure_ref") private String procedureRef;
  @Column(name="control_ref") private String controlRef;
  @Column(name="evidence_ref") private String evidenceRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected LegalRequirement() {}
  public LegalRequirement(UUID facilityId, String governorate, String title, String sourceType, LocalDate effectiveFrom, LocalDate effectiveTo,
                          String policyRef, String procedureRef, String controlRef, String evidenceRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.sourceType=sourceType; this.effectiveFrom=effectiveFrom;
    this.effectiveTo=effectiveTo; this.policyRef=policyRef; this.procedureRef=procedureRef; this.controlRef=controlRef; this.evidenceRef=evidenceRef;
  }
  public UUID getId(){return id;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;}
}
