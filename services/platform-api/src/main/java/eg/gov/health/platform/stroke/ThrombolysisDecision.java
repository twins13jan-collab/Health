package eg.gov.health.platform.stroke;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "thrombolysis_decisions")
public class ThrombolysisDecision {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="activation_id", nullable=false) private UUID activationId;
  @Column(name="recent_surgery", nullable=false) private boolean recentSurgery = false;
  @Column(name="active_bleeding", nullable=false) private boolean activeBleeding = false;
  @Column(name="uncontrolled_bp", nullable=false) private boolean uncontrolledBp = false;
  @Column(name="anticoagulant_use", nullable=false) private boolean anticoagulantUse = false;
  @Column(name="low_glucose_mimic", nullable=false) private boolean lowGlucoseMimic = false;
  @Column(nullable=false) private String decision;
  private String reason; @Column(name="decided_by") private String decidedBy;
  @Column(name="needle_at") private OffsetDateTime needleAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ThrombolysisDecision() {}
  public ThrombolysisDecision(UUID facilityId, String governorate, UUID activationId, boolean recentSurgery, boolean activeBleeding,
                              boolean uncontrolledBp, boolean anticoagulantUse, boolean lowGlucoseMimic, String decision,
                              String reason, String decidedBy, OffsetDateTime needleAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.activationId=activationId; this.recentSurgery=recentSurgery;
    this.activeBleeding=activeBleeding; this.uncontrolledBp=uncontrolledBp; this.anticoagulantUse=anticoagulantUse;
    this.lowGlucoseMimic=lowGlucoseMimic; this.decision=decision; this.reason=reason; this.decidedBy=decidedBy; this.needleAt=needleAt;
  }
  public UUID getId(){return id;} public String getDecision(){return decision;}
}
