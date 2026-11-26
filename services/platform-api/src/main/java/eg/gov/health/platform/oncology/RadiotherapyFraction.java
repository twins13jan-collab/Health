package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "radiotherapy_fractions")
public class RadiotherapyFraction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(name="fraction_number", nullable=false) private int fractionNumber;
  @Column(name="delivered_dose") private String deliveredDose;
  @Column(name="verified_by") private String verifiedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RadiotherapyFraction() {}
  public RadiotherapyFraction(UUID facilityId, String governorate, UUID planId, int fractionNumber, String deliveredDose, String verifiedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.fractionNumber=fractionNumber; this.deliveredDose=deliveredDose; this.verifiedBy=verifiedBy;
  }
  public UUID getId(){return id;}
}
