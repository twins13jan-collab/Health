package eg.gov.health.platform.facilityenvironment;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "waste_manifests")
public class WasteManifest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="waste_type", nullable=false) private String wasteType;
  @Column(name="source_unit") private String sourceUnit;
  @Column(name="weight_kg") private BigDecimal weightKg;
  private Integer containers;
  @Column(name="generated_at") private OffsetDateTime generatedAt;
  @Column(name="generated_by") private String generatedBy;
  private String contractor; @Column(name="contractor_license") private String contractorLicense;
  @Column(name="license_valid_until") private LocalDate licenseValidUntil;
  @Column(name="collected_at") private OffsetDateTime collectedAt;
  @Column(name="collected_by") private String collectedBy;
  @Column(name="received_by") private String receivedBy;
  @Column(name="disposed_at") private OffsetDateTime disposedAt;
  @Column(name="disposal_method") private String disposalMethod;
  @Column(nullable=false) private String status = "generated";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WasteManifest() {}
  public WasteManifest(UUID facilityId, String governorate, String wasteType, String sourceUnit, BigDecimal weightKg, Integer containers, String generatedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.wasteType=wasteType; this.sourceUnit=sourceUnit;
    this.weightKg=weightKg; this.containers=containers; this.generatedBy=generatedBy; this.generatedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getWasteType(){return wasteType;}
  public String getStatus(){return status;} public OffsetDateTime getGeneratedAt(){return generatedAt;}
  public String getGeneratedBy(){return generatedBy;} public String getCollectedBy(){return collectedBy;} public String getReceivedBy(){return receivedBy;}
  public void collect(String contractor, String license, LocalDate licenseUntil, String collectedBy, OffsetDateTime at){
    this.contractor=contractor; this.contractorLicense=license; this.licenseValidUntil=licenseUntil;
    this.collectedBy=collectedBy; this.collectedAt=at; this.status="collected"; this.updatedAt=OffsetDateTime.now(); }
  public void dispose(String receivedBy, String method, OffsetDateTime at){
    this.receivedBy=receivedBy; this.disposalMethod=method; this.disposedAt=at; this.status="disposed"; this.updatedAt=OffsetDateTime.now(); }
}
