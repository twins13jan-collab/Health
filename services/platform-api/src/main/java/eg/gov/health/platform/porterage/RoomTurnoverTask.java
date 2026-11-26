package eg.gov.health.platform.porterage;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "room_turnover_tasks")
public class RoomTurnoverTask {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="room_ref") private String roomRef;
  @Column(name="discharged_at") private OffsetDateTime dischargedAt;
  @Column(name="cleaning_done", nullable=false) private boolean cleaningDone = false;
  @Column(name="bed_ready", nullable=false) private boolean bedReady = false;
  @Column(name="inspected_by") private String inspectedBy;
  @Column(name="ready_at") private OffsetDateTime readyAt;
  @Column(nullable=false) private String status = "pending";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RoomTurnoverTask() {}
  public RoomTurnoverTask(UUID facilityId, String governorate, String roomRef, OffsetDateTime dischargedAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.roomRef=roomRef; this.dischargedAt=dischargedAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public String getRoomRef(){return roomRef;} public OffsetDateTime getDischargedAt(){return dischargedAt;}
  public boolean isCleaningDone(){return cleaningDone;} public boolean isBedReady(){return bedReady;} public String getInspectedBy(){return inspectedBy;}
  public OffsetDateTime getReadyAt(){return readyAt;} public OffsetDateTime getCreatedAt(){return createdAt;}
  public void progress(boolean cleaning, boolean bed, String inspector){
    this.cleaningDone=cleaning; this.bedReady=bed; this.inspectedBy=inspector;
    if (PorterageRules.roomReady(cleaning, bed, inspector)) { this.status="ready"; this.readyAt=OffsetDateTime.now(); }
    else this.status="cleaning";
    this.updatedAt=OffsetDateTime.now(); }
}
