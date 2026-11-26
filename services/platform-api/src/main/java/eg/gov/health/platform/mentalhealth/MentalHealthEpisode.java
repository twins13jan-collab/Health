package eg.gov.health.platform.mentalhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "mental_health_episodes")
public class MentalHealthEpisode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="referral_reason", nullable=false) private String referralReason;
  @Column(nullable=false) private String urgency = "routine";
  private String provider;
  @Column(name="diagnosis_code") private String diagnosisCode; // restricted — see controller access rules
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MentalHealthEpisode() {}
  public MentalHealthEpisode(UUID facilityId, String governorate, UUID patientId, String referralReason, String urgency, String provider, String diagnosisCode) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.referralReason=referralReason;
    if (urgency!=null) this.urgency=urgency; this.provider=provider; this.diagnosisCode=diagnosisCode;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public String getReferralReason(){return referralReason;} public String getUrgency(){return urgency;}
  public String getStatus(){return status;} public String getDiagnosisCode(){return diagnosisCode;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
