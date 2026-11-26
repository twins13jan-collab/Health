package eg.gov.health.platform.infectioncontrol;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "outbreak_investigations")
public class OutbreakInvestigation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title; private String unit;
  @Column(name="suspected_organism") private String suspectedOrganism;
  @Column(name="cases_count", nullable=false) private int casesCount = 0;
  @Column(name="control_measures") private String controlMeasures;
  @Column(name="opened_by") private String openedBy;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected OutbreakInvestigation() {}
  public OutbreakInvestigation(UUID facilityId, String governorate, String title, String unit, String suspectedOrganism, int casesCount, String controlMeasures, String openedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.unit=unit; this.suspectedOrganism=suspectedOrganism;
    this.casesCount=casesCount; this.controlMeasures=controlMeasures; this.openedBy=openedBy;
  }
  public UUID getId(){return id;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
