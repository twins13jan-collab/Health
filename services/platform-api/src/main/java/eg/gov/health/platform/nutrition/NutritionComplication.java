package eg.gov.health.platform.nutrition;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nutrition_complications")
public class NutritionComplication {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="order_id") private UUID orderId;
  @Column(nullable=false) private String type;
  private String severity;
  @Column(name="action_taken") private String actionTaken;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NutritionComplication() {}
  public NutritionComplication(UUID facilityId, String governorate, UUID patientId, UUID orderId, String type, String severity, String actionTaken) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.orderId=orderId;
    this.type=type; this.severity=severity; this.actionTaken=actionTaken;
  }
  public UUID getId(){return id;} public String getType(){return type;}
}
