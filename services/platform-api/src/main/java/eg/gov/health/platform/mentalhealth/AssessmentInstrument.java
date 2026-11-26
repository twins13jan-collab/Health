package eg.gov.health.platform.mentalhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "assessment_instruments")
public class AssessmentInstrument {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="episode_id", nullable=false) private UUID episodeId;
  @Column(nullable=false) private String instrument;
  @Column(name="instrument_version", nullable=false) private String instrumentVersion;
  @Column(name="total_score", nullable=false) private int totalScore;
  private String severity;
  @Column(name="item9_positive", nullable=false) private boolean item9Positive = false;
  private String assessor;
  @Column(name="assessed_at") private OffsetDateTime assessedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AssessmentInstrument() {}
  public AssessmentInstrument(UUID facilityId, String governorate, UUID episodeId, String instrument, String version,
                              int totalScore, boolean item9Positive, String assessor) {
    this.facilityId=facilityId; this.governorate=governorate; this.episodeId=episodeId; this.instrument=instrument;
    this.instrumentVersion=version; this.totalScore=totalScore;
    this.severity = MentalHealthRules.severityFor(instrument, totalScore);
    this.item9Positive=item9Positive; this.assessor=assessor;
  }
  public UUID getId(){return id;} public String getSeverity(){return severity;} public boolean isItem9Positive(){return item9Positive;}
}
