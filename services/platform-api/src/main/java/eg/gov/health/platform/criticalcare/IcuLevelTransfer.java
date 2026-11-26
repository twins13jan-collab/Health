package eg.gov.health.platform.criticalcare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "icu_level_transfers")
public class IcuLevelTransfer {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="from_level") private String fromLevel;
  @Column(name="to_level") private String toLevel;
  @Column(name="criteria_met", nullable=false) private boolean criteriaMet = false;
  @Column(name="stability_reviewed", nullable=false) private boolean stabilityReviewed = false;
  @Column(name="team_equipped", nullable=false) private boolean teamEquipped = false;
  @Column(name="handover_documented", nullable=false) private boolean handoverDocumented = false;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IcuLevelTransfer() {}
  public IcuLevelTransfer(UUID facilityId, String governorate, String patientRef, String fromLevel, String toLevel, boolean criteriaMet,
                            boolean stabilityReviewed, boolean teamEquipped, boolean handoverDocumented) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.fromLevel=fromLevel; this.toLevel=toLevel;
    this.criteriaMet=criteriaMet; this.stabilityReviewed=stabilityReviewed; this.teamEquipped=teamEquipped; this.handoverDocumented=handoverDocumented;
  }
  public UUID getId(){return id;} public boolean isCriteriaMet(){return criteriaMet;} public boolean isStabilityReviewed(){return stabilityReviewed;}
  public boolean isTeamEquipped(){return teamEquipped;} public boolean isHandoverDocumented(){return handoverDocumented;}
  public void complete(){ this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
}
