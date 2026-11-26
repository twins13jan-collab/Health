package eg.gov.health.platform.disasterops;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "situation_reports")
public class SituationReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="event_id", nullable=false) private UUID eventId;
  private Integer casualties;
  @Column(name="beds_available") private Integer bedsAvailable;
  @Column(name="teams_deployed") private Integer teamsDeployed;
  @Column(name="stock_notes") private String stockNotes;
  @Column(name="roads_notes") private String roadsNotes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SituationReport() {}
  public SituationReport(UUID facilityId, String governorate, UUID eventId, Integer casualties, Integer bedsAvailable,
                         Integer teamsDeployed, String stockNotes, String roadsNotes) {
    this.facilityId=facilityId; this.governorate=governorate; this.eventId=eventId; this.casualties=casualties; this.bedsAvailable=bedsAvailable;
    this.teamsDeployed=teamsDeployed; this.stockNotes=stockNotes; this.roadsNotes=roadsNotes;
  }
  public UUID getId(){return id;}
}
