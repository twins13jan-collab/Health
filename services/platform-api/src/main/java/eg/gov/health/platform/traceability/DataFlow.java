package eg.gov.health.platform.traceability;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "data_flows")
public class DataFlow {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="data_entity") private String dataEntity;
  @Column(name="producer_module") private String producerModule;
  @Column(name="consumer_module") private String consumerModule;
  @Column(name="owner_module") private String ownerModule;
  @Column(nullable=false) private boolean authorized = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DataFlow() {}
  public DataFlow(UUID facilityId, String governorate, String dataEntity, String producerModule, String consumerModule, String ownerModule, boolean authorized) {
    this.facilityId=facilityId; this.governorate=governorate; this.dataEntity=dataEntity; this.producerModule=producerModule;
    this.consumerModule=consumerModule; this.ownerModule=ownerModule; this.authorized=authorized;
  }
  public UUID getId(){return id;} public String getDataEntity(){return dataEntity;} public String getOwnerModule(){return ownerModule;}
}
