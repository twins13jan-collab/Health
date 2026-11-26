package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "surgical_cases")
public class SurgicalCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="procedure_name", nullable=false) private String procedureName;
  @Column(name="operating_room") private String operatingRoom;
  @Column(name="room_id") private UUID roomId;
  @Column(name="status_reason") private String statusReason;
  @Column(name="scheduled_at") private OffsetDateTime scheduledAt;
  @Column(nullable=false) private String checklistStage = "sign_in"; // sign_in, time_out, sign_out, completed
  @Column(name="confirmed_items") private String confirmedItems = "";
  @Column(nullable=false) private String status = "scheduled"; // scheduled, in_progress, completed, postponed, cancelled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SurgicalCase() {}
  public SurgicalCase(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String procedureName, String operatingRoom, OffsetDateTime scheduledAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.procedureName=procedureName; this.operatingRoom=operatingRoom; this.scheduledAt=scheduledAt;
  }
  public UUID getId(){return id;} public String getChecklistStage(){return checklistStage;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public String getProcedureName(){return procedureName;} public String getOperatingRoom(){return operatingRoom;}
  public OffsetDateTime getScheduledAt(){return scheduledAt;} public UUID getRoomId(){return roomId;}
  public String getStatusReason(){return statusReason;}
  public void setRoomId(UUID r){this.roomId=r; this.updatedAt=OffsetDateTime.now();}
  public void setStatusReason(String r){this.statusReason=r; this.updatedAt=OffsetDateTime.now();}
  public void setScheduledAt(OffsetDateTime t){this.scheduledAt=t; this.updatedAt=OffsetDateTime.now();}
  public java.util.Set<String> confirmedSet() {
    return confirmedItems==null||confirmedItems.isBlank() ? java.util.Set.of() : java.util.Set.of(confirmedItems.split(","));
  }
  public void confirmItems(java.util.Collection<String> items) {
    var s = new java.util.TreeSet<>(confirmedSet()); s.addAll(items); this.confirmedItems = String.join(",", s); this.updatedAt=OffsetDateTime.now();
  }
  public void advanceTo(String stage){ this.checklistStage=stage; if ("completed".equals(stage)) this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
