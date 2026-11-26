package eg.gov.health.platform.painsedation;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "analgesia_plans")
public class AnalgesiaPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="plan_type") private String planType;
  @Column(name="controlled_medication", nullable=false) private boolean controlledMedication = false;
  @Column(name="prescribed_by") private String prescribedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AnalgesiaPlan() {}
  public AnalgesiaPlan(UUID facilityId, String governorate, String patientRef, String planType, boolean controlledMedication, String prescribedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.planType=planType;
    this.controlledMedication=controlledMedication; this.prescribedBy=prescribedBy;
  }
  public UUID getId(){return id;}
}
