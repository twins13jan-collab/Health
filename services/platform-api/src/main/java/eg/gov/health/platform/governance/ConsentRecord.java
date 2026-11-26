package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "consent_records")
public class ConsentRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String purpose;   // treatment, disclosure, research, communication, imaging, secondary_use
  @Column(nullable=false) private String recipient;
  @Column(name="data_scope", nullable=false) private String dataScope;
  @Column(name="consent_version") private String consentVersion;
  @Column(nullable=false) private String status = "active"; // active, revoked, expired
  @Column(name="effective_from") private OffsetDateTime effectiveFrom = OffsetDateTime.now();
  @Column(name="expires_at") private OffsetDateTime expiresAt;
  @Column(name="revoked_at") private OffsetDateTime revokedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ConsentRecord() {}
  public ConsentRecord(UUID facilityId, String governorate, UUID patientId, String purpose, String recipient, String dataScope, String consentVersion, OffsetDateTime expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.purpose=purpose;
    this.recipient=recipient; this.dataScope=dataScope; this.consentVersion=consentVersion; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public String getPurpose(){return purpose;}
  public String getRecipient(){return recipient;} public String getDataScope(){return dataScope;} public String getStatus(){return status;}
  public OffsetDateTime getEffectiveFrom(){return effectiveFrom;} public OffsetDateTime getExpiresAt(){return expiresAt;}
  public void revoke(){this.status="revoked"; this.revokedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now();}
}
