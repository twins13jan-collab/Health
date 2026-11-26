package eg.gov.health.platform.rehab;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "outcome_measures")
public class OutcomeMeasure {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  @Column(nullable=false) private String instrument;
  @Column(name="instrument_version", nullable=false) private String instrumentVersion;
  @Column(nullable=false) private double score;
  private String assessor;
  @Column(name="measured_at") private OffsetDateTime measuredAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OutcomeMeasure() {}
  public OutcomeMeasure(UUID facilityId, String governorate, UUID planId, String instrument, String version, double score, String assessor) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.instrument=instrument;
    this.instrumentVersion=version; this.score=score; this.assessor=assessor;
  }
  public UUID getId(){return id;} public double getScore(){return score;} public String getInstrument(){return instrument;}
}
