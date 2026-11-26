package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "referrals")
public class Referral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="to_facility_id", nullable=false) private UUID toFacilityId;
  @Column(nullable=false) private String reason;
  @Column(nullable=false) private String urgency = "routine"; // routine, urgent, emergency
  @Column(nullable=false) private String status = "pending"; // pending, accepted, rejected, in_transit, arrived, completed, cancelled
  @Column(name="accepted_by") private String acceptedBy;
  @Column(name="rejection_reason") private String rejectionReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Referral() {}
  public Referral(UUID facilityId, String governorate, UUID patientId, UUID toFacilityId, String reason, String urgency) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.toFacilityId=toFacilityId;
    this.reason=reason; this.urgency=urgency==null?"routine":urgency;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;}
  public UUID getToFacilityId(){return toFacilityId;} public UUID getPatientId(){return patientId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public void accept(String by){this.status="accepted"; this.acceptedBy=by; this.updatedAt=OffsetDateTime.now();}
  public void reject(String reason){this.status="rejected"; this.rejectionReason=reason; this.updatedAt=OffsetDateTime.now();}
}
