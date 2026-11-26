package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mapping_rules")
public class MappingRule {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="endpoint_id", nullable=false) private UUID endpointId;
  @Column(name="source_field") private String sourceField; @Column(name="target_field") private String targetField;
  @Column(nullable=false) private int version = 1;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MappingRule() {}
  public MappingRule(UUID facilityId, String governorate, UUID endpointId, String sourceField, String targetField, int version) {
    this.facilityId=facilityId; this.governorate=governorate; this.endpointId=endpointId; this.sourceField=sourceField;
    this.targetField=targetField; this.version=version;
  }
  public UUID getId(){return id;}
}
