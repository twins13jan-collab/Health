package eg.gov.health.platform.complexcare;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "care_transitions")
public class CareTransition {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="from_setting") private String fromSetting;
  @Column(name="to_setting") private String toSetting;
  @Column(name="handover_documented", nullable=false) private boolean handoverDocumented = false;
  @Column(name="equipment_needed", nullable=false) private boolean equipmentNeeded = false;
  @Column(name="equipment_arranged", nullable=false) private boolean equipmentArranged = false;
  @Column(name="transition_at") private OffsetDateTime transitionAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CareTransition() {}
  public CareTransition(UUID facilityId, String governorate, UUID planId, String fromSetting, String toSetting,
                        boolean handoverDocumented, boolean equipmentNeeded, boolean equipmentArranged) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.fromSetting=fromSetting; this.toSetting=toSetting;
    this.handoverDocumented=handoverDocumented; this.equipmentNeeded=equipmentNeeded; this.equipmentArranged=equipmentArranged;
    this.transitionAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
