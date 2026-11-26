package eg.gov.health.platform.stroke;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "stroke_monitoring")
public class StrokeMonitoring {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="activation_id", nullable=false) private UUID activationId;
  @Column(name="swallow_screen") private String swallowScreen;
  @Column(name="oral_intake", nullable=false) private boolean oralIntake = false;
  private String consciousness; private String mobility;
  @Column(name="hemorrhage_flag", nullable=false) private boolean hemorrhageFlag = false;
  private String notes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected StrokeMonitoring() {}
  public StrokeMonitoring(UUID facilityId, String governorate, UUID activationId, String swallowScreen, boolean oralIntake, String consciousness, String mobility, boolean hemorrhageFlag, String notes) {
    this.facilityId=facilityId; this.governorate=governorate; this.activationId=activationId; this.swallowScreen=swallowScreen;
    this.oralIntake=oralIntake; this.consciousness=consciousness; this.mobility=mobility; this.hemorrhageFlag=hemorrhageFlag; this.notes=notes;
  }
  public UUID getId(){return id;}
}
