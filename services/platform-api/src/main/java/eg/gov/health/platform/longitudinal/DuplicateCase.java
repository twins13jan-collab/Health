package eg.gov.health.platform.longitudinal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "duplicate_cases")
public class DuplicateCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="identity_a", nullable=false) private UUID identityA;
  @Column(name="identity_b", nullable=false) private UUID identityB;
  @Column(name="similarity_score", nullable=false) private java.math.BigDecimal similarityScore = java.math.BigDecimal.ZERO;
  @Column(name="similarity_reasons") private String similarityReasons;
  @Column(name="decided_by") private String decidedBy;
  @Column(name="second_approver") private String secondApprover;
  @Column(name="decision_evidence") private String decisionEvidence;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DuplicateCase() {}
  public DuplicateCase(UUID facilityId, String governorate, UUID identityA, UUID identityB, java.math.BigDecimal similarityScore, String similarityReasons) {
    this.facilityId=facilityId; this.governorate=governorate; this.identityA=identityA; this.identityB=identityB;
    this.similarityScore=similarityScore; this.similarityReasons=similarityReasons;
  }
  public UUID getId(){return id;} public UUID getIdentityA(){return identityA;} public UUID getIdentityB(){return identityB;}
  public java.math.BigDecimal getSimilarityScore(){return similarityScore;} public String getStatus(){return status;}
  public void decide(String status, String by, String second, String evidence) {
    this.status=status; this.decidedBy=by; this.secondApprover=second; this.decisionEvidence=evidence; this.updatedAt=OffsetDateTime.now();
  }
}
