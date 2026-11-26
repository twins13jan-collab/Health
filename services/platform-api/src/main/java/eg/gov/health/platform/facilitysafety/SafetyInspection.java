package eg.gov.health.platform.facilitysafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "safety_inspections")
public class SafetyInspection {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="system_type", nullable=false) private String systemType;
  private String location; private String result; private String defects;
  @Column(name="inspected_by") private String inspectedBy;
  @Column(name="next_due") private LocalDate nextDue;
  @Column(name="corrective_action") private String correctiveAction;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SafetyInspection() {}
  public SafetyInspection(UUID facilityId, String governorate, String systemType, String location, String result, String defects, String inspectedBy, LocalDate nextDue, String correctiveAction) {
    this.facilityId=facilityId; this.governorate=governorate; this.systemType=systemType; this.location=location; this.result=result;
    this.defects=defects; this.inspectedBy=inspectedBy; this.nextDue=nextDue; this.correctiveAction=correctiveAction;
  }
  public UUID getId(){return id;}
}
