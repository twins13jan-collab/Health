package eg.gov.health.platform.geodata;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "facility_locations")
public class FacilityLocation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="facility_ref") private String facilityRef;
  private String name;
  private BigDecimal latitude; private BigDecimal longitude;
  @Column(name="accuracy_m") private BigDecimal accuracyM;
  @Column(name="verified_at") private LocalDate verifiedAt;
  @Column(name="verified_by") private String verifiedBy;
  private String district;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FacilityLocation() {}
  public FacilityLocation(UUID facilityId, String governorate, String facilityRef, String name, BigDecimal latitude, BigDecimal longitude,
                          BigDecimal accuracyM, LocalDate verifiedAt, String verifiedBy, String district) {
    this.facilityId=facilityId; this.governorate=governorate; this.facilityRef=facilityRef; this.name=name; this.latitude=latitude;
    this.longitude=longitude; this.accuracyM=accuracyM; this.verifiedAt=verifiedAt; this.verifiedBy=verifiedBy; this.district=district;
  }
  public UUID getId(){return id;} public BigDecimal getLatitude(){return latitude;} public BigDecimal getLongitude(){return longitude;}
  public BigDecimal getAccuracyM(){return accuracyM;} public LocalDate getVerifiedAt(){return verifiedAt;}
}
