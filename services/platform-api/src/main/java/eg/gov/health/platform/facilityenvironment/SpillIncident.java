package eg.gov.health.platform.facilityenvironment;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "spill_incidents")
public class SpillIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String substance; private String location; private String severity;
  @Column(name="reported_by") private String reportedBy;
  @Column(name="reported_at") private OffsetDateTime reportedAt;
  @Column(name="contained_at") private OffsetDateTime containedAt;
  @Column(nullable=false) private boolean decontaminated = false;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SpillIncident() {}
  public SpillIncident(UUID facilityId, String governorate, String substance, String location, String severity, String reportedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.substance=substance; this.location=location;
    this.severity=severity; this.reportedBy=reportedBy; this.reportedAt=OffsetDateTime.now(); this.status="responding";
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void contain(OffsetDateTime at, boolean decontaminated){ this.containedAt=at; this.decontaminated=decontaminated; this.status="contained"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
  public OffsetDateTime getContainedAt(){return containedAt;} public boolean isDecontaminated(){return decontaminated;}
}
