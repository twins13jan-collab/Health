package eg.gov.health.platform.majorincidents;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "major_incident_plans")
public class MajorIncidentPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="agencies_involved") private String agenciesInvolved;
  @Column(name="command_chain") private String commandChain;
  @Column(name="alternative_comms") private String alternativeComms;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MajorIncidentPlan() {}
  public MajorIncidentPlan(UUID facilityId, String governorate, String name, String agenciesInvolved, String commandChain, String alternativeComms) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.agenciesInvolved=agenciesInvolved;
    this.commandChain=commandChain; this.alternativeComms=alternativeComms;
  }
  public UUID getId(){return id;} public String getCommandChain(){return commandChain;} public String getAlternativeComms(){return alternativeComms;} public String getStatus(){return status;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
