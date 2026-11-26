package eg.gov.health.platform.complexcare;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "individual_emergency_plans")
public class IndividualEmergencyPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  private String triggers; private String actions; private String contacts;
  @Column(name="reviewed_at") private LocalDate reviewedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IndividualEmergencyPlan() {}
  public IndividualEmergencyPlan(UUID facilityId, String governorate, UUID planId, String triggers, String actions, String contacts, LocalDate reviewedAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.triggers=triggers; this.actions=actions;
    this.contacts=contacts; this.reviewedAt=reviewedAt;
  }
  public UUID getId(){return id;}
}
