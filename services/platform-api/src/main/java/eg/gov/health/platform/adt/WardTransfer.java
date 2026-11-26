package eg.gov.health.platform.adt;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ward_transfers")
public class WardTransfer {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="admission_id", nullable=false) private UUID admissionId;
  @Column(name="from_ward_id") private UUID fromWardId;
  @Column(name="to_ward_id", nullable=false) private UUID toWardId;
  @Column(name="from_bed") private String fromBed;
  @Column(name="to_bed", nullable=false) private String toBed;
  @Column(nullable=false) private String reason;
  @Column(name="transferred_by") private String transferredBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected WardTransfer() {}
  public WardTransfer(UUID facilityId, String governorate, UUID admissionId, UUID fromWardId, UUID toWardId,
                      String fromBed, String toBed, String reason, String transferredBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.admissionId=admissionId;
    this.fromWardId=fromWardId; this.toWardId=toWardId; this.fromBed=fromBed; this.toBed=toBed;
    this.reason=reason; this.transferredBy=transferredBy;
  }
  public UUID getId(){return id;} public UUID getAdmissionId(){return admissionId;}
  public UUID getFromWardId(){return fromWardId;} public UUID getToWardId(){return toWardId;}
  public String getFromBed(){return fromBed;} public String getToBed(){return toBed;}
  public String getReason(){return reason;} public OffsetDateTime getCreatedAt(){return createdAt;}
}
