package eg.gov.health.platform.emergencytriage;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "er_departures")
public class ErDeparture {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  @Column(name="departure_type") private String departureType;
  @Column(name="risks_explained", nullable=false) private boolean risksExplained = false;
  @Column(nullable=false) private boolean signed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ErDeparture() {}
  public ErDeparture(UUID facilityId, String governorate, UUID assessmentId, String departureType, boolean risksExplained, boolean signed) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.departureType=departureType;
    this.risksExplained=risksExplained; this.signed=signed;
  }
  public UUID getId(){return id;}
}
