package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cancer_cases")
public class CancerCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="tumor_type") private String tumorType;
  private String stage; private String biomarkers;
  @Column(name="diagnosed_by") private String diagnosedBy;
  @Column(nullable=false) private String status = "diagnosed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CancerCase() {}
  public CancerCase(UUID facilityId, String governorate, String patientRef, String tumorType, String stage, String biomarkers, String diagnosedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.tumorType=tumorType;
    this.stage=stage; this.biomarkers=biomarkers; this.diagnosedBy=diagnosedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
