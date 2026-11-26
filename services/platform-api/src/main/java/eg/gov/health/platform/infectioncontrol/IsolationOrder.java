package eg.gov.health.platform.infectioncontrol;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "isolation_orders")
public class IsolationOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="case_id") private UUID caseId;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="precaution_type", nullable=false) private String precautionType;
  private String location; @Column(name="location_type") private String locationType;
  @Column(name="ppe_required") private String ppeRequired;
  @Column(name="ordered_by") private String orderedBy;
  @Column(name="discontinued_by") private String discontinuedBy;
  @Column(name="discontinued_at") private OffsetDateTime discontinuedAt;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IsolationOrder() {}
  public IsolationOrder(UUID facilityId, String governorate, UUID caseId, String patientRef, String precautionType, String location, String locationType, String ppeRequired, String orderedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.caseId=caseId; this.patientRef=patientRef;
    this.precautionType=precautionType; this.location=location; this.locationType=locationType; this.ppeRequired=ppeRequired; this.orderedBy=orderedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void discontinue(String by){ this.discontinuedBy=by; this.discontinuedAt=OffsetDateTime.now(); this.status="discontinued"; this.updatedAt=OffsetDateTime.now(); }
}
