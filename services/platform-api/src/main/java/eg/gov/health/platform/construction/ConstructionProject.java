package eg.gov.health.platform.construction;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "construction_projects")
public class ConstructionProject {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String location;
  @Column(name="need_study_ref") private String needStudyRef;
  @Column(name="health_map_ref") private String healthMapRef;
  @Column(nullable=false) private String status = "planning";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ConstructionProject() {}
  public ConstructionProject(UUID facilityId, String governorate, String name, String location, String needStudyRef, String healthMapRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.location=location; this.needStudyRef=needStudyRef; this.healthMapRef=healthMapRef;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getNeedStudyRef(){return needStudyRef;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
