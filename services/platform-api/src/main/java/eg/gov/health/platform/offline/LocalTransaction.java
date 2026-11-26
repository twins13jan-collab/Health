package eg.gov.health.platform.offline;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "local_transactions")
public class LocalTransaction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="package_id", nullable=false) private UUID packageId;
  @Column(name="local_id", nullable=false) private String localId;
  @Column(name="operation_type", nullable=false) private String operationType;
  @Column(nullable=false) private String domain = "administrative";
  @Column(name="payload_hash") private String payloadHash;
  @Column(name="client_time") private OffsetDateTime clientTime;
  @Column(name="reference_data_version", nullable=false) private int referenceDataVersion = 1;
  @Column(name="conflicts_with_server", nullable=false) private boolean conflictsWithServer = false;
  @Column(nullable=false) private String status = "pending";
  @Column(name="server_seq") private Long serverSeq;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected LocalTransaction() {}
  public LocalTransaction(UUID facilityId, String governorate, UUID packageId, String localId, String operationType, String domain,
                          String payloadHash, OffsetDateTime clientTime, int referenceDataVersion, boolean conflictsWithServer) {
    this.facilityId=facilityId; this.governorate=governorate; this.packageId=packageId; this.localId=localId;
    this.operationType=operationType; this.domain=domain==null?"administrative":domain; this.payloadHash=payloadHash;
    this.clientTime=clientTime; this.referenceDataVersion=referenceDataVersion; this.conflictsWithServer=conflictsWithServer;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getOperationType(){return operationType;}
  public String getDomain(){return domain;} public int getReferenceDataVersion(){return referenceDataVersion;}
  public boolean isConflictsWithServer(){return conflictsWithServer;} public String getLocalId(){return localId;}
  public void setStatus(String s){ this.status=s; }
  public void setServerSeq(Long s){ this.serverSeq=s; }
}
