package eg.gov.health.platform.facilitysafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "evacuation_drills")
public class EvacuationDrill {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String building; @Column(name="drill_date") private LocalDate drillDate;
  private Integer participants; @Column(name="evacuation_minutes") private BigDecimal evacuationMinutes;
  private String result; private String deficiencies;
  @Column(name="conducted_by") private String conductedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EvacuationDrill() {}
  public EvacuationDrill(UUID facilityId, String governorate, String building, LocalDate drillDate, Integer participants, BigDecimal evacuationMinutes, String deficiencies, String conductedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.building=building; this.drillDate=drillDate; this.participants=participants;
    this.evacuationMinutes=evacuationMinutes; this.deficiencies=deficiencies; this.conductedBy=conductedBy;
    this.result = evacuationMinutes != null && evacuationMinutes.compareTo(new BigDecimal("8")) <= 0 ? "pass" : "fail";
  }
  public UUID getId(){return id;} public String getResult(){return result;}
}
