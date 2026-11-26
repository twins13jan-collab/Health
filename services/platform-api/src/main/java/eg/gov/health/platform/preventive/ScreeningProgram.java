package eg.gov.health.platform.preventive;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "screening_programs")
public class ScreeningProgram {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="target_condition") private String targetCondition;
  @Column(name="eligibility_criteria") private String eligibilityCriteria;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ScreeningProgram() {}
  public ScreeningProgram(UUID facilityId, String governorate, String name, String targetCondition, String eligibilityCriteria) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.targetCondition=targetCondition; this.eligibilityCriteria=eligibilityCriteria;
  }
  public UUID getId(){return id;}
}
