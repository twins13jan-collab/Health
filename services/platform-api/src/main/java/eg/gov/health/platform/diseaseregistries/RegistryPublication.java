package eg.gov.health.platform.diseaseregistries;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "registry_publications")
public class RegistryPublication {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="registry_id", nullable=false) private UUID registryId;
  private String title;
  @Column(name="bias_note") private String biasNote;
  @Column(name="lost_rate_pct") private Double lostRatePct;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RegistryPublication() {}
  public RegistryPublication(UUID facilityId, String governorate, UUID registryId, String title, String biasNote, Double lostRatePct) {
    this.facilityId=facilityId; this.governorate=governorate; this.registryId=registryId; this.title=title; this.biasNote=biasNote; this.lostRatePct=lostRatePct;
  }
  public UUID getId(){return id;}
}
