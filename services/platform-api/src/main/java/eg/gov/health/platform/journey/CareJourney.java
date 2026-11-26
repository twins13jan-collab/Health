package eg.gov.health.platform.journey;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "care_journeys")
public class CareJourney {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="correlation_id", nullable=false) private String correlationId;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CareJourney() {}
  public CareJourney(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String correlationId) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId; this.correlationId=correlationId;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public UUID getEncounterId(){return encounterId;}
  public String getCorrelationId(){return correlationId;} public String getStatus(){return status;}
  public void complete(){ this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
