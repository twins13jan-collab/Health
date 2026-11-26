package eg.gov.health.platform.servicecatalog;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "catalog_changes")
public class CatalogChange {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_id", nullable=false) private UUID serviceId;
  @Column(name="change_description") private String changeDescription;
  @Column(name="approved_by") private String approvedBy;
  @Column(name="effective_from") private LocalDate effectiveFrom;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CatalogChange() {}
  public CatalogChange(UUID facilityId, String governorate, UUID serviceId, String changeDescription, String approvedBy, LocalDate effectiveFrom) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceId=serviceId; this.changeDescription=changeDescription;
    this.approvedBy=approvedBy; this.effectiveFrom=effectiveFrom;
  }
  public UUID getId(){return id;}
}
