package eg.gov.health.platform.eligibility;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "service_availability_checks")
public class ServiceAvailabilityCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="service_code") private String serviceCode;
  @Column(name="service_name") private String serviceName;
  @Column(name="slot_at") private OffsetDateTime slotAt;
  @Column(name="service_offered", nullable=false) private boolean serviceOffered = false;
  @Column(name="license_valid", nullable=false) private boolean licenseValid = false;
  @Column(name="capacity_available", nullable=false) private boolean capacityAvailable = false;
  @Column(name="staff_available", nullable=false) private boolean staffAvailable = false;
  @Column(name="equipment_available", nullable=false) private boolean equipmentAvailable = false;
  @Column(name="consumables_available", nullable=false) private boolean consumablesAvailable = false;
  @Column(name="preparation_met", nullable=false) private boolean preparationMet = false;
  @Column(name="clinical_eligible", nullable=false) private boolean clinicalEligible = false;
  @Column(name="coverage_status") private String coverageStatus;
  private String result; private String restrictions;
  @Column(name="checked_by") private String checkedBy;
  private String source;
  @Column(name="checked_at", nullable=false) private OffsetDateTime checkedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ServiceAvailabilityCheck() {}
  public ServiceAvailabilityCheck(UUID facilityId, String governorate, String patientRef, String serviceCode, String serviceName, OffsetDateTime slotAt,
                                  boolean offered, boolean license, boolean capacity, boolean staff, boolean equipment, boolean consumables,
                                  boolean preparation, boolean clinical, String coverageStatus, String result, String restrictions, String checkedBy, String source) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.serviceCode=serviceCode; this.serviceName=serviceName;
    this.slotAt=slotAt; this.serviceOffered=offered; this.licenseValid=license; this.capacityAvailable=capacity; this.staffAvailable=staff;
    this.equipmentAvailable=equipment; this.consumablesAvailable=consumables; this.preparationMet=preparation; this.clinicalEligible=clinical;
    this.coverageStatus=coverageStatus; this.result=result; this.restrictions=restrictions; this.checkedBy=checkedBy; this.source=source;
  }
  public UUID getId(){return id;} public String getResult(){return result;} public String getRestrictions(){return restrictions;}
}
