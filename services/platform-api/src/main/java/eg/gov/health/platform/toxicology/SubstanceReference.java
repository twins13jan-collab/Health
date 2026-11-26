package eg.gov.health.platform.toxicology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "substance_references")
public class SubstanceReference {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String name;
  private String concentration; private String packaging;
  @Column(nullable=false) private String source;
  @Column(name="review_date", nullable=false) private LocalDate reviewDate;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SubstanceReference() {}
  public SubstanceReference(UUID facilityId, String governorate, String name, String concentration, String packaging, String source, LocalDate reviewDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.concentration=concentration;
    this.packaging=packaging; this.source=source; this.reviewDate=reviewDate;
  }
  public UUID getId(){return id;} public boolean isActive(){return active;} public LocalDate getReviewDate(){return reviewDate;}
}
