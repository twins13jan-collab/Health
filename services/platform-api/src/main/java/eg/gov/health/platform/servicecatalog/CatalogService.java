package eg.gov.health.platform.servicecatalog;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "catalog_services")
public class CatalogService {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String code; private String name; private String category;
  @Column(name="resource_requirements") private String resourceRequirements;
  @Column(name="delivery_location") private String deliveryLocation;
  private Double price;
  @Column(name="insurance_coverage") private String insuranceCoverage;
  @Column(name="effective_from") private LocalDate effectiveFrom;
  @Column(name="effective_to") private LocalDate effectiveTo;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CatalogService() {}
  public CatalogService(UUID facilityId, String governorate, String code, String name, String category, String resourceRequirements,
                          String deliveryLocation, Double price, String insuranceCoverage, LocalDate effectiveFrom, LocalDate effectiveTo) {
    this.facilityId=facilityId; this.governorate=governorate; this.code=code; this.name=name; this.category=category;
    this.resourceRequirements=resourceRequirements; this.deliveryLocation=deliveryLocation; this.price=price;
    this.insuranceCoverage=insuranceCoverage; this.effectiveFrom=effectiveFrom; this.effectiveTo=effectiveTo;
  }
  public UUID getId(){return id;} public String getCode(){return code;} public String getResourceRequirements(){return resourceRequirements;}
  public String getDeliveryLocation(){return deliveryLocation;} public LocalDate getEffectiveFrom(){return effectiveFrom;}
  public LocalDate getEffectiveTo(){return effectiveTo;} public String getStatus(){return status;}
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
}
