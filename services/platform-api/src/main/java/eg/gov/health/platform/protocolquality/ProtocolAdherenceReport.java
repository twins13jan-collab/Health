package eg.gov.health.platform.protocolquality;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "protocol_adherence_reports")
public class ProtocolAdherenceReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="protocol_id", nullable=false) private UUID protocolId;
  @Column(name="total_cases") private Integer totalCases;
  @Column(name="adherent_cases") private Integer adherentCases;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ProtocolAdherenceReport() {}
  public ProtocolAdherenceReport(UUID facilityId, String governorate, UUID protocolId, Integer totalCases, Integer adherentCases) {
    this.facilityId=facilityId; this.governorate=governorate; this.protocolId=protocolId; this.totalCases=totalCases; this.adherentCases=adherentCases;
  }
  public UUID getId(){return id;}
}
