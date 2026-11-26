package eg.gov.health.platform.porterage;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "transport_requests")
public class TransportRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="from_location") private String fromLocation;
  @Column(name="to_location") private String toLocation;
  private String priority;
  @Column(name="escort_required", nullable=false) private boolean escortRequired = false;
  @Column(name="oxygen_required", nullable=false) private boolean oxygenRequired = false;
  @Column(name="escort_name") private String escortName;
  @Column(name="assigned_porter") private String assignedPorter;
  @Column(name="requested_at", nullable=false) private OffsetDateTime requestedAt = OffsetDateTime.now();
  @Column(name="picked_up_at") private OffsetDateTime pickedUpAt;
  @Column(name="delivered_at") private OffsetDateTime deliveredAt;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TransportRequest() {}
  public TransportRequest(UUID facilityId, String governorate, String patientRef, String fromLocation, String toLocation, String priority,
                            boolean escortRequired, boolean oxygenRequired, OffsetDateTime requestedAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.fromLocation=fromLocation; this.toLocation=toLocation;
    this.priority=priority; this.escortRequired=escortRequired; this.oxygenRequired=oxygenRequired;
    if (requestedAt!=null) this.requestedAt=requestedAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getPriority(){return priority;}
  public String getPatientRef(){return patientRef;} public String getFromLocation(){return fromLocation;} public String getToLocation(){return toLocation;}
  public boolean isEscortRequired(){return escortRequired;} public String getEscortName(){return escortName;} public String getAssignedPorter(){return assignedPorter;}
  public OffsetDateTime getPickedUpAt(){return pickedUpAt;} public OffsetDateTime getCreatedAt(){return createdAt;}
  public boolean isOxygenRequired(){return oxygenRequired;} public OffsetDateTime getRequestedAt(){return requestedAt;} public OffsetDateTime getDeliveredAt(){return deliveredAt;}
  public void assign(String porter, String escort){ this.assignedPorter=porter; this.escortName=escort; this.status="assigned"; this.updatedAt=OffsetDateTime.now(); }
  public void pickup(){ this.pickedUpAt=OffsetDateTime.now(); this.status="in_transit"; this.updatedAt=OffsetDateTime.now(); }
  public void deliver(OffsetDateTime at){ this.deliveredAt=at==null?OffsetDateTime.now():at; this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
