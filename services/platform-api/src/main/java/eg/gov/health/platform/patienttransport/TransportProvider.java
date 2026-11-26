package eg.gov.health.platform.patienttransport;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "transport_providers")
public class TransportProvider {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(nullable=false) private boolean verified = false;
  @Column(name="licence_valid_until") private LocalDate licenceValidUntil;
  @Column(name="oxygen_capable", nullable=false) private boolean oxygenCapable = false;
  @Column(name="stretcher_capable", nullable=false) private boolean stretcherCapable = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TransportProvider() {}
  public TransportProvider(UUID facilityId, String governorate, String name, boolean verified, LocalDate licenceValidUntil,
                           boolean oxygenCapable, boolean stretcherCapable) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.verified=verified; this.licenceValidUntil=licenceValidUntil;
    this.oxygenCapable=oxygenCapable; this.stretcherCapable=stretcherCapable;
  }
  public UUID getId(){return id;} public boolean isVerified(){return verified;} public LocalDate getLicenceValidUntil(){return licenceValidUntil;}
  public boolean isOxygenCapable(){return oxygenCapable;} public boolean isStretcherCapable(){return stretcherCapable;}
}
