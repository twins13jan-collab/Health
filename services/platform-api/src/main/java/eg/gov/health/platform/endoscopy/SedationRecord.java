package eg.gov.health.platform.endoscopy;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sedation_records")
public class SedationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="procedure_id", nullable=false) private UUID procedureId;
  private String drugs; private String monitoring;
  @Column(name="aldrete_score") private Integer aldreteScore;
  @Column(name="discharge_decision") private String dischargeDecision;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SedationRecord() {}
  public SedationRecord(UUID facilityId, String governorate, UUID procedureId, String drugs, String monitoring, Integer aldreteScore, String dischargeDecision) {
    this.facilityId=facilityId; this.governorate=governorate; this.procedureId=procedureId; this.drugs=drugs;
    this.monitoring=monitoring; this.aldreteScore=aldreteScore; this.dischargeDecision=dischargeDecision;
  }
  public UUID getId(){return id;}
}
