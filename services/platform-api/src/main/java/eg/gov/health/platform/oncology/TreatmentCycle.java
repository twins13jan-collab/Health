package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "treatment_cycles")
public class TreatmentCycle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id", nullable=false) private UUID caseId;
  @Column(name="protocol_version_id", nullable=false) private UUID protocolVersionId;
  @Column(name="cycle_number", nullable=false) private int cycleNumber = 1;
  private String drugs; @Column(name="approved_dose") private String approvedDose;
  @Column(name="prescribed_by") private String prescribedBy;
  @Column(nullable=false) private boolean postponed = false;
  @Column(name="postpone_reason") private String postponeReason;
  @Column(name="postpone_impact") private String postponeImpact;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TreatmentCycle() {}
  public TreatmentCycle(UUID facilityId, String governorate, UUID caseId, UUID protocolVersionId, int cycleNumber, String drugs, String approvedDose, String prescribedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.protocolVersionId=protocolVersionId;
    this.cycleNumber=cycleNumber; this.drugs=drugs; this.approvedDose=approvedDose; this.prescribedBy=prescribedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public String getPrescribedBy(){return prescribedBy;} public UUID getCaseId(){return caseId;}
  public UUID getProtocolVersionId(){return protocolVersionId;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void postpone(String reason, String impact){ this.postponed=true; this.postponeReason=reason; this.postponeImpact=impact; this.status="postponed"; this.updatedAt=OffsetDateTime.now(); }
}
