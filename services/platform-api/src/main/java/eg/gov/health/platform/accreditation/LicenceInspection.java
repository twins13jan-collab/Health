package eg.gov.health.platform.accreditation;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "licence_inspections")
public class LicenceInspection {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="licence_id", nullable=false) private UUID licenceId;
  private String inspector; private String findings;
  @Column(name="corrective_actions") private String correctiveActions;
  @Column(name="corrective_done", nullable=false) private boolean correctiveDone = false;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LicenceInspection() {}
  public LicenceInspection(UUID facilityId, String governorate, UUID licenceId, String inspector, String findings, String correctiveActions) {
    this.facilityId=facilityId; this.governorate=governorate; this.licenceId=licenceId; this.inspector=inspector; this.findings=findings;
    this.correctiveActions=correctiveActions;
  }
  public UUID getId(){return id;} public String getFindings(){return findings;} public boolean isCorrectiveDone(){return correctiveDone;}
  public void completeCorrective(){ this.correctiveDone=true; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
