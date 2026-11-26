package eg.gov.health.platform.facilityenvironment;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "cleaning_audits")
public class CleaningAudit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id") private UUID planId;
  private String area; @Column(name="score_pct") private BigDecimal scorePct;
  private Boolean passed; private String auditor; private String findings;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CleaningAudit() {}
  public CleaningAudit(UUID facilityId, String governorate, UUID planId, String area, BigDecimal scorePct, String auditor, String findings) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.area=area; this.scorePct=scorePct;
    this.passed = scorePct != null && scorePct.compareTo(new BigDecimal("85")) >= 0; this.auditor=auditor; this.findings=findings;
  }
  public UUID getId(){return id;} public Boolean getPassed(){return passed;}
}
