package eg.gov.health.platform.unknownpatient;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "identity_merge_records")
public class IdentityMergeRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="temp_id", nullable=false) private UUID tempId;
  @Column(name="verified_patient_ref") private String verifiedPatientRef;
  @Column(name="verification_evidence") private String verificationEvidence;
  @Column(name="verified_by") private String verifiedBy;
  @Column(name="merged_by") private String mergedBy;
  @Column(name="merged_at") private OffsetDateTime mergedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected IdentityMergeRecord() {}
  public IdentityMergeRecord(UUID facilityId, String governorate, UUID tempId, String verifiedPatientRef, String verificationEvidence, String verifiedBy, String mergedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.tempId=tempId; this.verifiedPatientRef=verifiedPatientRef;
    this.verificationEvidence=verificationEvidence; this.verifiedBy=verifiedBy; this.mergedBy=mergedBy; this.mergedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;}
}
