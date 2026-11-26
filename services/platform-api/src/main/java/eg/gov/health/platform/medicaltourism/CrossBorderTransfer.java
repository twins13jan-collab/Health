package eg.gov.health.platform.medicaltourism;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "cross_border_transfers")
public class CrossBorderTransfer {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id", nullable=false) private UUID requestId;
  @Column(name="data_description") private String dataDescription;
  @Column(name="legal_basis") private String legalBasis;
  @Column(name="patient_consent", nullable=false) private boolean patientConsent = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CrossBorderTransfer() {}
  public CrossBorderTransfer(UUID facilityId, String governorate, UUID requestId, String dataDescription, String legalBasis, boolean patientConsent) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.dataDescription=dataDescription;
    this.legalBasis=legalBasis; this.patientConsent=patientConsent;
  }
  public UUID getId(){return id;}
}
