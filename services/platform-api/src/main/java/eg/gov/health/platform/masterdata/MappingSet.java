package eg.gov.health.platform.masterdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mapping_sets")
public class MappingSet {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="local_code", nullable=false) private String localCode;
  @Column(name="standard_code", nullable=false) private String standardCode;
  @Column(nullable=false) private String equivalence = "exact";
  @Column(name="conversion_reason") private String conversionReason;
  @Column(nullable=false) private int version = 1;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MappingSet() {}
  public MappingSet(UUID facilityId, String governorate, String localCode, String standardCode, String equivalence, String conversionReason) {
    this.facilityId=facilityId; this.governorate=governorate; this.localCode=localCode; this.standardCode=standardCode;
    this.equivalence=equivalence==null?"exact":equivalence; this.conversionReason=conversionReason;
  }
  public UUID getId(){return id;}
}
