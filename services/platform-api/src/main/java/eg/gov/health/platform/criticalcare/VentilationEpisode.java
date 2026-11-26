package eg.gov.health.platform.criticalcare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ventilation_episodes")
public class VentilationEpisode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="stay_id", nullable=false) private UUID stayId;
  @Column(name="device_code", nullable=false) private String deviceCode;
  @Column(nullable=false) private String mode;
  private Double fio2; private Double peep;
  @Column(name="tidal_volume_ml") private Double tidalVolumeMl;
  @Column(name="rate_per_min") private Double ratePerMin;
  @Column(nullable=false) private String status = "active";
  @Column(name="started_at") private OffsetDateTime startedAt = OffsetDateTime.now();
  @Column(name="ended_at") private OffsetDateTime endedAt;
  @Column(name="started_by") private String startedBy;
  private String notes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected VentilationEpisode() {}
  public VentilationEpisode(UUID facilityId, String governorate, UUID stayId, String deviceCode, String mode,
                            Double fio2, Double peep, Double tidalVolumeMl, Double ratePerMin, String startedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.stayId=stayId; this.deviceCode=deviceCode; this.mode=mode;
    this.fio2=fio2; this.peep=peep; this.tidalVolumeMl=tidalVolumeMl; this.ratePerMin=ratePerMin; this.startedBy=startedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public String getDeviceCode(){return deviceCode;} public String getMode(){return mode;}
  public Double getFio2(){return fio2;} public OffsetDateTime getStartedAt(){return startedAt;}
  public void end(){ this.status="ended"; this.endedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
}
