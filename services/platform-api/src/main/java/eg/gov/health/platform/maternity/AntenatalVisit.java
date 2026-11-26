package eg.gov.health.platform.maternity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "antenatal_visits")
public class AntenatalVisit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="pregnancy_id", nullable=false) private UUID pregnancyId;
  @Column(name="visit_date", nullable=false) private LocalDate visitDate = LocalDate.now();
  @Column(name="gestational_weeks") private Integer gestationalWeeks;
  @Column(name="bp_systolic") private Integer bpSystolic;
  @Column(name="bp_diastolic") private Integer bpDiastolic;
  @Column(name="weight_kg") private Double weightKg;
  @Column(name="fundal_height_cm") private Double fundalHeightCm;
  @Column(name="fetal_heart_rate") private Integer fetalHeartRate;
  @Column(nullable=false) private boolean proteinuria = false;
  private String symptoms; @Column(name="labs_summary") private String labsSummary;
  private String education; private String plan;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AntenatalVisit() {}
  public AntenatalVisit(UUID facilityId, String governorate, UUID pregnancyId, Integer gestationalWeeks, Integer sys, Integer dia,
                        Double weightKg, Double fundal, Integer fhr, boolean proteinuria, String symptoms, String labs, String education, String plan) {
    this.facilityId=facilityId; this.governorate=governorate; this.pregnancyId=pregnancyId; this.gestationalWeeks=gestationalWeeks;
    this.bpSystolic=sys; this.bpDiastolic=dia; this.weightKg=weightKg; this.fundalHeightCm=fundal; this.fetalHeartRate=fhr;
    this.proteinuria=proteinuria; this.symptoms=symptoms; this.labsSummary=labs; this.education=education; this.plan=plan;
  }
  public UUID getId(){return id;}
  public Integer getBpSystolic(){return bpSystolic;} public Integer getBpDiastolic(){return bpDiastolic;}
  public LocalDate getVisitDate(){return visitDate;} public Integer getGestationalWeeks(){return gestationalWeeks;}
}
