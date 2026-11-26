package eg.gov.health.platform.patientsafety;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "incident_analyses")
public class IncidentAnalysis {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(name="systemic_causes") private String systemicCauses;
  @Column(name="human_factors") private String humanFactors;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IncidentAnalysis() {}
  public IncidentAnalysis(UUID facilityId, String governorate, UUID incidentId, String systemicCauses, String humanFactors) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.systemicCauses=systemicCauses; this.humanFactors=humanFactors;
  }
  public UUID getId(){return id;}
}
