package eg.gov.health.platform.maternity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "pregnancy_episodes")
public class PregnancyEpisode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="lmp_date") private LocalDate lmpDate;
  private LocalDate edd;
  @Column(name="dating_source") private String datingSource;
  @Column(name="fetus_count", nullable=false) private int fetusCount = 1;
  @Column(name="risk_level", nullable=false) private String riskLevel = "low";
  @Column(name="risk_factors") private String riskFactors;
  @Column(nullable=false) private String status = "ongoing";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PregnancyEpisode() {}
  public PregnancyEpisode(UUID facilityId, String governorate, UUID patientId, LocalDate lmpDate, String datingSource, int fetusCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.lmpDate=lmpDate;
    this.edd = MaternityRules.edd(lmpDate); this.datingSource=datingSource; this.fetusCount=fetusCount;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public LocalDate getLmpDate(){return lmpDate;} public LocalDate getEdd(){return edd;}
  public String getStatus(){return status;} public String getRiskLevel(){return riskLevel;} public int getFetusCount(){return fetusCount;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
  public void confirmRisk(String level, String factors){ this.riskLevel=level; this.riskFactors=factors; this.updatedAt=OffsetDateTime.now(); }
}
