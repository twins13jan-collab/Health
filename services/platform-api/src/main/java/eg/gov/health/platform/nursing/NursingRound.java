package eg.gov.health.platform.nursing;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A nursing observation round: vitals + pain + mobility with the
 *  NEWS2 score computed on the server at recording time, so the
 *  ward board always shows a trustworthy early-warning score. */
@Entity @Table(name = "nursing_rounds")
public class NursingRound {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(name = "admission_id") private UUID admissionId;
  @Column(name = "round_at", nullable = false) private OffsetDateTime roundAt = OffsetDateTime.now();
  @Column(name = "respiratory_rate") private Integer respiratoryRate;
  private Integer spo2;
  @Column(name = "on_oxygen", nullable = false) private boolean onOxygen = false;
  @Column(name = "systolic_bp") private Integer systolicBp;
  private Integer pulse;
  private Double temperature;
  private String consciousness;
  @Column(name = "pain_score") private Integer painScore;
  private String mobility;
  @Column(name = "news2_total") private Integer news2Total;
  @Column(name = "news2_band") private String news2Band;
  private String note;
  @Column(name = "recorded_by") private String recordedBy;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected NursingRound() {}
  public NursingRound(UUID facilityId, String governorate, UUID patientId, UUID admissionId,
                      Integer respiratoryRate, Integer spo2, boolean onOxygen, Integer systolicBp,
                      Integer pulse, Double temperature, String consciousness, Integer painScore,
                      String mobility, Integer news2Total, String news2Band, String note, String recordedBy) {
    this.facilityId = facilityId; this.governorate = governorate; this.patientId = patientId;
    this.admissionId = admissionId; this.respiratoryRate = respiratoryRate; this.spo2 = spo2;
    this.onOxygen = onOxygen; this.systolicBp = systolicBp; this.pulse = pulse;
    this.temperature = temperature; this.consciousness = consciousness; this.painScore = painScore;
    this.mobility = mobility; this.news2Total = news2Total; this.news2Band = news2Band;
    this.note = note; this.recordedBy = recordedBy;
  }
  public UUID getId() { return id; }
  public UUID getPatientId() { return patientId; }
  public UUID getAdmissionId() { return admissionId; }
  public OffsetDateTime getRoundAt() { return roundAt; }
  public Integer getRespiratoryRate() { return respiratoryRate; }
  public Integer getSpo2() { return spo2; }
  public boolean isOnOxygen() { return onOxygen; }
  public Integer getSystolicBp() { return systolicBp; }
  public Integer getPulse() { return pulse; }
  public Double getTemperature() { return temperature; }
  public String getConsciousness() { return consciousness; }
  public Integer getPainScore() { return painScore; }
  public String getMobility() { return mobility; }
  public Integer getNews2Total() { return news2Total; }
  public String getNews2Band() { return news2Band; }
  public String getNote() { return note; }
  public String getRecordedBy() { return recordedBy; }
}
