package eg.gov.health.platform.mentalhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "substance_use_episodes")
public class SubstanceUseEpisode {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="episode_id") private UUID episodeId;
  @Column(nullable=false) private String substance;
  @Column(name="use_pattern") private String usePattern;
  @Column(name="ciwa_score") private Integer ciwaScore;
  @Column(name="withdrawal_band") private String withdrawalBand;
  private String intervention;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SubstanceUseEpisode() {}
  public SubstanceUseEpisode(UUID facilityId, String governorate, UUID patientId, UUID episodeId, String substance,
                             String usePattern, Integer ciwaScore, String intervention) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.episodeId=episodeId;
    this.substance=substance; this.usePattern=usePattern; this.ciwaScore=ciwaScore;
    this.withdrawalBand = ciwaScore==null ? null : MentalHealthRules.ciwaBand(ciwaScore);
    this.intervention=intervention;
  }
  public UUID getId(){return id;} public String getWithdrawalBand(){return withdrawalBand;}
}
