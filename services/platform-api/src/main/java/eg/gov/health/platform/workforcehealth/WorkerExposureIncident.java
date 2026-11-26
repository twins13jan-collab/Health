package eg.gov.health.platform.workforcehealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "worker_exposure_incidents")
public class WorkerExposureIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_ref") private String workerRef;
  @Column(name="exposure_type") private String exposureType;
  @Column(name="investigation_done", nullable=false) private boolean investigationDone = false;
  @Column(name="follow_up_done", nullable=false) private boolean followUpDone = false;
  @Column(name="compensation_filed", nullable=false) private boolean compensationFiled = false;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WorkerExposureIncident() {}
  public WorkerExposureIncident(UUID facilityId, String governorate, String workerRef, String exposureType) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerRef=workerRef; this.exposureType=exposureType;
  }
  public UUID getId(){return id;} public boolean isInvestigationDone(){return investigationDone;} public boolean isFollowUpDone(){return followUpDone;} public String getStatus(){return status;}
  public void investigate(){ this.investigationDone=true; this.updatedAt=OffsetDateTime.now(); }
  public void followUp(){ this.followUpDone=true; this.updatedAt=OffsetDateTime.now(); }
  public void fileCompensation(){ this.compensationFiled=true; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
