package eg.gov.health.platform.dental;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dental_chart_entries")
public class DentalChartEntry {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="chart_id", nullable=false) private UUID chartId;
  @Column(name="tooth_number", nullable=false) private int toothNumber;
  private String condition; private String surfaces; private String notes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DentalChartEntry() {}
  public DentalChartEntry(UUID facilityId, String governorate, UUID chartId, int toothNumber, String condition, String surfaces, String notes) {
    this.facilityId=facilityId; this.governorate=governorate; this.chartId=chartId; this.toothNumber=toothNumber;
    this.condition=condition; this.surfaces=surfaces; this.notes=notes;
  }
  public UUID getId(){return id;}
}
