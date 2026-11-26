package eg.gov.health.platform.diseaseregistries;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "registry_cases")
public class RegistryCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="registry_id", nullable=false) private UUID registryId;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="meets_definition", nullable=false) private boolean meetsDefinition = false;
  @Column(name="outcome_recorded", nullable=false) private boolean outcomeRecorded = false;
  @Column(name="followup_status", nullable=false) private String followupStatus = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RegistryCase() {}
  public RegistryCase(UUID facilityId, String governorate, UUID registryId, String patientRef, boolean meetsDefinition, boolean outcomeRecorded, String followupStatus) {
    this.facilityId=facilityId; this.governorate=governorate; this.registryId=registryId; this.patientRef=patientRef; this.meetsDefinition=meetsDefinition;
    this.outcomeRecorded=outcomeRecorded; this.followupStatus=followupStatus;
  }
  public UUID getId(){return id;} public String getFollowupStatus(){return followupStatus;}
}
