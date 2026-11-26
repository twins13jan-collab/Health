package eg.gov.health.platform.protocolquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "protocol_deviations")
public class ProtocolDeviation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="protocol_id", nullable=false) private UUID protocolId;
  @Column(name="deviation_type") private String deviationType;
  private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ProtocolDeviation() {}
  public ProtocolDeviation(UUID facilityId, String governorate, UUID protocolId, String deviationType, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.protocolId=protocolId; this.deviationType=deviationType; this.reason=reason;
  }
  public UUID getId(){return id;} public String getDeviationType(){return deviationType;}
}
