package eg.gov.health.platform.wounds;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "wound_cases")
public class WoundCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  private String cause; private String site;
  @Column(name="duration_days") private Integer durationDays;
  @Column(nullable=false) private String classification;
  @Column(name="burn_degree") private Integer burnDegree;
  @Column(name="tbsa_percent") private Double tbsaPercent;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WoundCase() {}
  public WoundCase(UUID facilityId, String governorate, UUID patientId, String cause, String site, Integer durationDays,
                   String classification, Integer burnDegree, Double tbsaPercent) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.cause=cause; this.site=site;
    this.durationDays=durationDays; this.classification=classification; this.burnDegree=burnDegree; this.tbsaPercent=tbsaPercent;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public Double getTbsaPercent(){return tbsaPercent;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
