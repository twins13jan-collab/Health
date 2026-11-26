package eg.gov.health.platform.cssd;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "set_usage_links")
public class SetUsageLink {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="set_id", nullable=false) private UUID setId;
  @Column(name="procedure_ref") private String procedureRef;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="used_at") private OffsetDateTime usedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SetUsageLink() {}
  public SetUsageLink(UUID facilityId, String governorate, UUID setId, String procedureRef, String patientRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.setId=setId; this.procedureRef=procedureRef; this.patientRef=patientRef; this.usedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public UUID getSetId(){return setId;}
  public String getProcedureRef(){return procedureRef;} public String getPatientRef(){return patientRef;}
  public OffsetDateTime getUsedAt(){return usedAt;}
}
