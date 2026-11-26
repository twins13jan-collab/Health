package eg.gov.health.platform.facilityenvironment;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "cleaning_plans")
public class CleaningPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String area; @Column(name="risk_level") private String riskLevel;
  private String frequency; private String responsible;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CleaningPlan() {}
  public CleaningPlan(UUID facilityId, String governorate, String area, String riskLevel, String frequency, String responsible) {
    this.facilityId=facilityId; this.governorate=governorate; this.area=area; this.riskLevel=riskLevel; this.frequency=frequency; this.responsible=responsible;
  }
  public UUID getId(){return id;}
}
