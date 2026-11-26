package eg.gov.health.platform.protocolversions;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinical_protocol_usages")
public class ClinicalProtocolUsage {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="protocol_version_id", nullable=false) private UUID protocolVersionId;
  @Column(name="service_ref") private String serviceRef;
  @Column(name="service_date") private LocalDate serviceDate;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClinicalProtocolUsage() {}
  public ClinicalProtocolUsage(UUID facilityId, String governorate, UUID protocolVersionId, String serviceRef, LocalDate serviceDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.protocolVersionId=protocolVersionId; this.serviceRef=serviceRef; this.serviceDate=serviceDate;
  }
  public UUID getId(){return id;}
}
