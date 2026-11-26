package eg.gov.health.platform.envhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "env_data_feeds")
public class EnvDataFeed {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="source_type") private String sourceType;
  @Column(name="source_name") private String sourceName;
  @Column(nullable=false) private boolean reliable = false;
  @Column(name="last_value") private String lastValue;
  @Column(name="recorded_at") private OffsetDateTime recordedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EnvDataFeed() {}
  public EnvDataFeed(UUID facilityId, String governorate, String sourceType, String sourceName, boolean reliable, String lastValue) {
    this.facilityId=facilityId; this.governorate=governorate; this.sourceType=sourceType; this.sourceName=sourceName; this.reliable=reliable;
    this.lastValue=lastValue; this.recordedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public boolean isReliable(){return reliable;}
}
