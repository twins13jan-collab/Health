package eg.gov.health.platform.reports;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "indicator_definitions")
public class IndicatorDefinition {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String code;
  @Column(nullable=false) private String name;
  private String formula;
  @Column(name="numerator_def") private String numeratorDef;
  @Column(name="denominator_def") private String denominatorDef;
  private String exclusions; @Column(name="source_system") private String sourceSystem;
  private String owner;
  @Column(nullable=false) private int version = 1;
  @Column(nullable=false) private String status = "draft";
  @Column(name="effective_from") private LocalDate effectiveFrom;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IndicatorDefinition() {}
  public IndicatorDefinition(UUID facilityId, String governorate, String code, String name, String formula, String numeratorDef,
                             String denominatorDef, String exclusions, String sourceSystem, String owner, int version, LocalDate effectiveFrom) {
    this.facilityId=facilityId; this.governorate=governorate; this.code=code; this.name=name; this.formula=formula;
    this.numeratorDef=numeratorDef; this.denominatorDef=denominatorDef; this.exclusions=exclusions;
    this.sourceSystem=sourceSystem; this.owner=owner; this.version=version; this.effectiveFrom=effectiveFrom;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getCode(){return code;}
  public int getVersion(){return version;} public String getStatus(){return status;} public String getOwner(){return owner;}
  public void approve(){ this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
}
