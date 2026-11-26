package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "interface_contracts")
public class InterfaceContract {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="endpoint_id", nullable=false) private UUID endpointId;
  @Column(name="message_type", nullable=false) private String messageType;
  private String fields; @Column(name="validation_rules") private String validationRules;
  @Column(nullable=false) private int version = 1;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected InterfaceContract() {}
  public InterfaceContract(UUID facilityId, String governorate, UUID endpointId, String messageType, String fields, String validationRules, int version) {
    this.facilityId=facilityId; this.governorate=governorate; this.endpointId=endpointId; this.messageType=messageType;
    this.fields=fields; this.validationRules=validationRules; this.version=version;
  }
  public UUID getId(){return id;}
}
