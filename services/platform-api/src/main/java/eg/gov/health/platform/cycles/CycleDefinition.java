package eg.gov.health.platform.cycles;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name = "cycle_definitions")
public class CycleDefinition {
  @Id @GeneratedValue private UUID id;
  @Column(unique = true, nullable = false) private String code;
  @Column(name = "catalog_number", unique = true, nullable = false) private int catalogNumber;
  @Column(name = "title_ar", nullable = false) private String titleAr;
  @Column(nullable = false) private String domain;
  @Column(name = "specialised_route") private String specialisedRoute;
  @Column(name = "sla_hours", nullable = false) private int slaHours = 72;
  @Column(nullable = false) private String status = "active";
  protected CycleDefinition() {}
  public String getCode() { return code; } public int getCatalogNumber() { return catalogNumber; }
  public String getTitleAr() { return titleAr; } public String getDomain() { return domain; }
  public String getSpecialisedRoute() { return specialisedRoute; } public int getSlaHours() { return slaHours; }
  public String getStatus() { return status; } public UUID getId() { return id; }
}
