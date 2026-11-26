package eg.gov.health.platform.diagnostics;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "radiology_orders")
public class RadiologyOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(nullable=false) private String modality; // XR, CT, MRI, US, MAMMO, ANGIO
  @Column(name="body_part") private String bodyPart;
  @Column(name="clinical_question", nullable=false) private String clinicalQuestion;
  @Column(nullable=false) private String priority = "routine";
  @Column(nullable=false) private String status = "ordered"; // ordered, scheduled, in_progress, imaged, reported, verified, cancelled
  @Column(name="report_text") private String reportText;
  @Column(name="reported_by") private String reportedBy;
  @Column(name="ordered_by") private String orderedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RadiologyOrder() {}
  public RadiologyOrder(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String modality, String bodyPart, String clinicalQuestion, String priority, String orderedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.modality=modality; this.bodyPart=bodyPart; this.clinicalQuestion=clinicalQuestion;
    this.priority=priority==null?"routine":priority; this.orderedBy=orderedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;}
  public String getModality(){return modality;} public UUID getPatientId(){return patientId;}
  public UUID getEncounterId(){return encounterId;} public String getBodyPart(){return bodyPart;}
  public String getClinicalQuestion(){return clinicalQuestion;} public String getPriority(){return priority;}
  public String getReportText(){return reportText;} public String getReportedBy(){return reportedBy;}
  public String getOrderedBy(){return orderedBy;}
  public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public void report(String text, String by){this.reportText=text; this.reportedBy=by; this.status="reported"; this.updatedAt=OffsetDateTime.now();}
}
