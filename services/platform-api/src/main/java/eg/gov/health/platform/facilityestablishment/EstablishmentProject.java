package eg.gov.health.platform.facilityestablishment;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "establishment_projects")
public class EstablishmentProject {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="needs_study_done", nullable=false) private boolean needsStudyDone = false;
  @Column(name="site_selected", nullable=false) private boolean siteSelected = false;
  @Column(name="infection_control_design_approved", nullable=false) private boolean infectionControlDesignApproved = false;
  @Column(nullable=false) private String status = "in_progress";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EstablishmentProject() {}
  public EstablishmentProject(UUID facilityId, String governorate, String name, boolean needsStudyDone, boolean siteSelected, boolean infectionControlDesignApproved) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.needsStudyDone=needsStudyDone; this.siteSelected=siteSelected;
    this.infectionControlDesignApproved=infectionControlDesignApproved;
  }
  public UUID getId(){return id;} public boolean isNeedsStudyDone(){return needsStudyDone;} public boolean isSiteSelected(){return siteSelected;}
  public boolean isInfectionControlDesignApproved(){return infectionControlDesignApproved;}
  public void open(){ this.status="opened"; this.updatedAt=OffsetDateTime.now(); }
}
