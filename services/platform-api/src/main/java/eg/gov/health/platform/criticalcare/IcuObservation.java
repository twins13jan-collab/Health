package eg.gov.health.platform.criticalcare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "icu_observations")
public class IcuObservation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="stay_id", nullable=false) private UUID stayId;
  @Column(nullable=false) private String kind;
  @Column(nullable=false) private double value;
  private String unit;
  @Column(name="device_code") private String deviceCode;
  @Column(name="source_time", nullable=false) private OffsetDateTime sourceTime;
  @Column(name="received_time") private OffsetDateTime receivedTime = OffsetDateTime.now();
  @Column(nullable=false) private String quality = "good";
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IcuObservation() {}
  public IcuObservation(UUID facilityId, String governorate, UUID stayId, String kind, double value, String unit,
                        String deviceCode, OffsetDateTime sourceTime, String quality, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.stayId=stayId; this.kind=kind; this.value=value;
    this.unit=unit; this.deviceCode=deviceCode; this.sourceTime=sourceTime; this.quality=quality; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;} public UUID getStayId(){return stayId;} public OffsetDateTime getSourceTime(){return sourceTime;}
  public String getQuality(){return quality;} public String getKind(){return kind;} public double getValue(){return value;}
}
