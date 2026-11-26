package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "assets")
public class Asset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_code", nullable=false, unique=true) private String assetCode;
  @Column(nullable=false) private String name;
  @Column(name="next_maintenance") private LocalDate nextMaintenance;
  @Column(nullable=false) private String status = "operational"; // operational, under_maintenance, out_of_service, retired
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Asset() {}
  public Asset(UUID facilityId, String governorate, String assetCode, String name, LocalDate nextMaintenance) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetCode=assetCode; this.name=name; this.nextMaintenance=nextMaintenance;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public LocalDate getNextMaintenance(){return nextMaintenance;}
  public void completeMaintenance(LocalDate next){this.nextMaintenance=next; this.status="operational"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
