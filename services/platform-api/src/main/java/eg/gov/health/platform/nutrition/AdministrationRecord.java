package eg.gov.health.platform.nutrition;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "administration_records")
public class AdministrationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  @Column(name="prescribed_ml", nullable=false) private double prescribedMl;
  @Column(name="actual_ml", nullable=false) private double actualMl;
  @Column(name="remaining_ml", nullable=false) private double remainingMl = 0;
  @Column(nullable=false) private boolean stopped = false;
  @Column(name="stop_reason") private String stopReason;
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AdministrationRecord() {}
  public AdministrationRecord(UUID facilityId, String governorate, UUID orderId, double prescribedMl, double actualMl,
                              double remainingMl, boolean stopped, String stopReason, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.prescribedMl=prescribedMl;
    this.actualMl=actualMl; this.remainingMl=remainingMl; this.stopped=stopped; this.stopReason=stopReason; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;} public double getPrescribedMl(){return prescribedMl;} public double getActualMl(){return actualMl;}
}
