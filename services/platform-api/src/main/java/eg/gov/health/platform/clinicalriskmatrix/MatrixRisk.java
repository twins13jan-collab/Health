package eg.gov.health.platform.clinicalriskmatrix;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "matrix_risks")
public class MatrixRisk {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="hazard_domain") private String hazardDomain;
  private String title; private Integer likelihood; private Integer severity;
  @Column(name="controls_prevention") private String controlsPrevention;
  @Column(name="controls_detection") private String controlsDetection;
  @Column(name="controls_response") private String controlsResponse;
  @Column(nullable=false) private boolean controlled = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MatrixRisk() {}
  public MatrixRisk(UUID facilityId, String governorate, String hazardDomain, String title, Integer likelihood, Integer severity,
                    String controlsPrevention, String controlsDetection, String controlsResponse) {
    this.facilityId=facilityId; this.governorate=governorate; this.hazardDomain=hazardDomain; this.title=title; this.likelihood=likelihood;
    this.severity=severity; this.controlsPrevention=controlsPrevention; this.controlsDetection=controlsDetection; this.controlsResponse=controlsResponse;
  }
  public UUID getId(){return id;} public Integer getLikelihood(){return likelihood;} public Integer getSeverity(){return severity;}
  public String getControlsPrevention(){return controlsPrevention;} public String getControlsDetection(){return controlsDetection;} public String getControlsResponse(){return controlsResponse;}
  public void markControlled(){ this.controlled=true; this.updatedAt=OffsetDateTime.now(); }
}
