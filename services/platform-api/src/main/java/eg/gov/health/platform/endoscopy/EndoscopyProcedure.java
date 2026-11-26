package eg.gov.health.platform.endoscopy;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "endoscopy_procedures")
public class EndoscopyProcedure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id", nullable=false) private UUID requestId;
  @Column(name="scope_id", nullable=false) private UUID scopeId;
  @Column(name="reprocessing_cycle_id") private UUID reprocessingCycleId;
  private String physician; private String sites; private String findings; private String interventions; private String complications;
  @Column(name="specimen_sent", nullable=false) private boolean specimenSent = false;
  @Column(name="specimen_label_verified", nullable=false) private boolean specimenLabelVerified = false;
  @Column(name="follow_up_plan") private String followUpPlan;
  @Column(name="discharge_instructions") private String dischargeInstructions;
  @Column(name="started_at") private OffsetDateTime startedAt = OffsetDateTime.now();
  @Column(name="ended_at") private OffsetDateTime endedAt;
  @Column(nullable=false) private String status = "in_progress";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EndoscopyProcedure() {}
  public EndoscopyProcedure(UUID facilityId, String governorate, UUID requestId, UUID scopeId, UUID reprocessingCycleId, String physician) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.scopeId=scopeId;
    this.reprocessingCycleId=reprocessingCycleId; this.physician=physician;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public UUID getRequestId(){return requestId;} public UUID getScopeId(){return scopeId;} public UUID getFacilityId(){return facilityId;}
  public void complete(String sites, String findings, String interventions, String complications, boolean specimenSent,
                       boolean labelVerified, String followUpPlan, String dischargeInstructions) {
    this.sites=sites; this.findings=findings; this.interventions=interventions; this.complications=complications;
    this.specimenSent=specimenSent; this.specimenLabelVerified=labelVerified; this.followUpPlan=followUpPlan;
    this.dischargeInstructions=dischargeInstructions; this.endedAt=OffsetDateTime.now();
    this.status = (complications!=null && !complications.isBlank()) ? "complicated" : "completed";
    this.updatedAt=OffsetDateTime.now();
  }
}
