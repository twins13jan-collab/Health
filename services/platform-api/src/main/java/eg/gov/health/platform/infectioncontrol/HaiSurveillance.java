package eg.gov.health.platform.infectioncontrol;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "hai_surveillance")
public class HaiSurveillance {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String unit; @Column(name="period_key") private String periodKey;
  @Column(name="hai_type") private String haiType;
  @Column(name="device_days", nullable=false) private int deviceDays = 0;
  @Column(name="infections_count", nullable=false) private int infectionsCount = 0;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected HaiSurveillance() {}
  public HaiSurveillance(UUID facilityId, String governorate, String unit, String periodKey, String haiType, int deviceDays, int infectionsCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.unit=unit; this.periodKey=periodKey; this.haiType=haiType;
    this.deviceDays=deviceDays; this.infectionsCount=infectionsCount;
  }
  public UUID getId(){return id;} public int getDeviceDays(){return deviceDays;} public int getInfectionsCount(){return infectionsCount;}
}
