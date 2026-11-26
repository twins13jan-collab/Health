package eg.gov.health.platform.dataquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity(name = "DqIndicatorDefinition") @Table(name = "dq_indicator_definitions")
public class DqIndicatorDefinition {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String definition; private String numerator; private String denominator;
  @Column(name="owner_ref") private String ownerRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DqIndicatorDefinition() {}
  public DqIndicatorDefinition(UUID facilityId, String governorate, String name, String definition, String numerator, String denominator, String ownerRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.definition=definition; this.numerator=numerator;
    this.denominator=denominator; this.ownerRef=ownerRef;
  }
  public UUID getId(){return id;}
}
