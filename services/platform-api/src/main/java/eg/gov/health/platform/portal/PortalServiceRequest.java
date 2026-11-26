package eg.gov.health.platform.portal;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

/** A service request from the patient portal to a facility:
 *  a lab test, a pharmacy refill, a home visit, or another
 *  service. It lands in the facility's inbox and moves
 *  requested -> confirmed -> done (or cancelled with a note). */
@Entity @Table(name = "portal_service_requests")
public class PortalServiceRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(name = "requested_by") private String requestedBy;
  @Column(name = "service_type", nullable = false) private String serviceType; // lab, pharmacy, home_visit, other
  private String details;
  @Column(name = "preferred_date") private LocalDate preferredDate;
  @Column(nullable = false) private String status = "requested";
  @Column(name = "staff_note") private String staffNote;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PortalServiceRequest() {}
  public PortalServiceRequest(UUID facilityId, String governorate, UUID patientId, String requestedBy,
                              String serviceType, String details, LocalDate preferredDate) {
    this.facilityId = facilityId; this.governorate = governorate; this.patientId = patientId;
    this.requestedBy = requestedBy; this.serviceType = serviceType;
    this.details = details; this.preferredDate = preferredDate;
  }
  public void setStatus(String s, String note) {
    this.status = s;
    if (note != null) this.staffNote = note;
    this.updatedAt = OffsetDateTime.now();
  }
  public UUID getId() { return id; }
  public UUID getFacilityId() { return facilityId; }
  public UUID getPatientId() { return patientId; }
  public String getServiceType() { return serviceType; }
  public String getDetails() { return details; }
  public LocalDate getPreferredDate() { return preferredDate; }
  public String getStatus() { return status; }
  public String getStaffNote() { return staffNote; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
