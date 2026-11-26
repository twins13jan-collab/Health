package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sterilization_cycles")
public class SterilizationCycle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="instrument_set", nullable=false) private String instrumentSet;
  @Column(name="cycle_number") private String cycleNumber;
  @Column(nullable=false) private String status;
  @Column(name="sterilized_at") private OffsetDateTime sterilizedAt = OffsetDateTime.now();
  @Column(name="expires_at", nullable=false) private OffsetDateTime expiresAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SterilizationCycle() {}
  public SterilizationCycle(UUID facilityId, String governorate, String instrumentSet, String cycleNumber, String status, OffsetDateTime expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.instrumentSet=instrumentSet;
    this.cycleNumber=cycleNumber; this.status=status; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getExpiresAt(){return expiresAt;}
}
