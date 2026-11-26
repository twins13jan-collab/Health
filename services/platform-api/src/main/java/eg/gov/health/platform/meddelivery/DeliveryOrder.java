package eg.gov.health.platform.meddelivery;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "delivery_orders")
public class DeliveryOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="prescription_ref") private String prescriptionRef;
  @Column(name="medication_name") private String medicationName;
  @Column(name="eligibility_verified", nullable=false) private boolean eligibilityVerified = false;
  @Column(name="pharmacist_reviewed_by") private String pharmacistReviewedBy;
  @Column(name="pharmacist_approved", nullable=false) private boolean pharmacistApproved = false;
  @Column(name="special_handling", nullable=false) private boolean specialHandling = false;
  @Column(name="cold_chain_required", nullable=false) private boolean coldChainRequired = false;
  @Column(name="address_ref") private String addressRef;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeliveryOrder() {}
  public DeliveryOrder(UUID facilityId, String governorate, String patientRef, String prescriptionRef, String medicationName,
                       boolean eligibilityVerified, boolean specialHandling, boolean coldChainRequired, String addressRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.prescriptionRef=prescriptionRef;
    this.medicationName=medicationName; this.eligibilityVerified=eligibilityVerified; this.specialHandling=specialHandling;
    this.coldChainRequired=coldChainRequired; this.addressRef=addressRef;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public boolean isEligibilityVerified(){return eligibilityVerified;} public boolean isPharmacistApproved(){return pharmacistApproved;}
  public boolean isSpecialHandling(){return specialHandling;} public boolean isColdChainRequired(){return coldChainRequired;}
  public void review(String pharmacist, boolean approved){ this.pharmacistReviewedBy=pharmacist; this.pharmacistApproved=approved; if (approved) this.status="reviewed"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
