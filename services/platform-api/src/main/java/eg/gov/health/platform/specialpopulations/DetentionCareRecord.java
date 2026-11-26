package eg.gov.health.platform.specialpopulations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "detention_care_records")
public class DetentionCareRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="point_id", nullable=false) private UUID pointId;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="legal_safeguards_confirmed", nullable=false) private boolean legalSafeguardsConfirmed = false;
  @Column(name="rights_informed", nullable=false) private boolean rightsInformed = false;
  @Column(name="care_summary") private String careSummary;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DetentionCareRecord() {}
  public DetentionCareRecord(UUID facilityId, String governorate, UUID pointId, String patientRef, boolean safeguards, boolean rights, String careSummary) {
    this.facilityId=facilityId; this.governorate=governorate; this.pointId=pointId; this.patientRef=patientRef;
    this.legalSafeguardsConfirmed=safeguards; this.rightsInformed=rights; this.careSummary=careSummary;
  }
  public UUID getId(){return id;} public boolean isLegalSafeguardsConfirmed(){return legalSafeguardsConfirmed;} public boolean isRightsInformed(){return rightsInformed;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
