package eg.gov.health.platform.auditcompliance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "conflict_disclosures")
public class ConflictDisclosure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="person_ref") private String personRef;
  private String interest;
  @Column(nullable=false) private boolean disclosed = false;
  @Column(name="management_plan") private String managementPlan;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ConflictDisclosure() {}
  public ConflictDisclosure(UUID facilityId, String governorate, String personRef, String interest, boolean disclosed, String managementPlan) {
    this.facilityId=facilityId; this.governorate=governorate; this.personRef=personRef; this.interest=interest;
    this.disclosed=disclosed; this.managementPlan=managementPlan;
  }
  public UUID getId(){return id;} public boolean isDisclosed(){return disclosed;}
}
