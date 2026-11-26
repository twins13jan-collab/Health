package eg.gov.health.platform.proms;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "prom_instruments")
public class PromInstrument {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String version; private String language;
  @Column(nullable=false) private boolean validated = false;
  private String domain;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PromInstrument() {}
  public PromInstrument(UUID facilityId, String governorate, String name, String version, String language, boolean validated, String domain) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.version=version; this.language=language; this.validated=validated; this.domain=domain;
  }
  public UUID getId(){return id;} public boolean isValidated(){return validated;}
}
