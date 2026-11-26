package eg.gov.health.platform.transfersafety;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "transfer_logs")
public class TransferLog {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  @Column(name="departed_at") private OffsetDateTime departedAt;
  @Column(name="arrived_at") private OffsetDateTime arrivedAt;
  @Column(name="handover_completed", nullable=false) private boolean handoverCompleted = false;
  @Column(name="handover_to") private String handoverTo;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TransferLog() {}
  public TransferLog(UUID facilityId, String governorate, UUID assessmentId, OffsetDateTime departedAt, OffsetDateTime arrivedAt,
                     boolean handoverCompleted, String handoverTo) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.departedAt=departedAt; this.arrivedAt=arrivedAt;
    this.handoverCompleted=handoverCompleted; this.handoverTo=handoverTo;
  }
  public UUID getId(){return id;}
}
