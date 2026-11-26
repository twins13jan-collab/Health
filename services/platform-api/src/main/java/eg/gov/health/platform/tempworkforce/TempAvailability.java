package eg.gov.health.platform.tempworkforce;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "temp_availability")
public class TempAvailability {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="provider_id", nullable=false) private UUID providerId;
  @Column(name="available_from") private OffsetDateTime availableFrom;
  @Column(name="available_to") private OffsetDateTime availableTo;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TempAvailability() {}
  public TempAvailability(UUID facilityId, String governorate, UUID providerId, OffsetDateTime availableFrom, OffsetDateTime availableTo) {
    this.facilityId=facilityId; this.governorate=governorate; this.providerId=providerId; this.availableFrom=availableFrom; this.availableTo=availableTo;
  }
  public UUID getId(){return id;}
}
