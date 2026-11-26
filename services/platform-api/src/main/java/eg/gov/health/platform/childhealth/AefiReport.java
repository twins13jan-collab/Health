package eg.gov.health.platform.childhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "aefi_reports")
public class AefiReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="dose_id", nullable=false) private UUID doseId;
  private String description;
  @Column(nullable=false) private boolean investigated = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AefiReport() {}
  public AefiReport(UUID facilityId, String governorate, UUID doseId, String description, boolean investigated) {
    this.facilityId=facilityId; this.governorate=governorate; this.doseId=doseId; this.description=description; this.investigated=investigated;
  }
  public UUID getId(){return id;} public boolean isInvestigated(){return investigated;}
  public void investigate(){ this.investigated=true; }
}
