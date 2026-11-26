package eg.gov.health.platform.journey;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "journey_steps")
public class JourneyStep {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="journey_id", nullable=false) private UUID journeyId;
  @Column(name="step_key", nullable=false) private String stepKey;
  private String module;
  @Column(name="entity_type") private String entityType;
  @Column(name="entity_id") private UUID entityId;
  private String detail;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected JourneyStep() {}
  public JourneyStep(UUID facilityId, String governorate, UUID journeyId, String stepKey, String module, String entityType, UUID entityId, String detail) {
    this.facilityId=facilityId; this.governorate=governorate; this.journeyId=journeyId; this.stepKey=stepKey; this.module=module;
    this.entityType=entityType; this.entityId=entityId; this.detail=detail;
  }
  public UUID getId(){return id;} public UUID getJourneyId(){return journeyId;} public String getStepKey(){return stepKey;}
  public String getModule(){return module;} public UUID getEntityId(){return entityId;}
}
