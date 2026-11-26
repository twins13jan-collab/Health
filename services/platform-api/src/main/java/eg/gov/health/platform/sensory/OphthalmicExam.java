package eg.gov.health.platform.sensory;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ophthalmic_exams")
public class OphthalmicExam {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String eye;
  @Column(name="visual_acuity") private Double visualAcuity;
  @Column(name="iop_mmhg") private Double iopMmhg;
  @Column(name="iop_suspect", nullable=false) private boolean iopSuspect = false;
  private String fundus;
  @Column(name="visual_field") private String visualField;
  private String examiner;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OphthalmicExam() {}
  public OphthalmicExam(UUID facilityId, String governorate, UUID patientId, String eye, Double visualAcuity, Double iopMmhg,
                        String fundus, String visualField, String examiner) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.eye=eye;
    this.visualAcuity=visualAcuity; this.iopMmhg=iopMmhg; this.iopSuspect=SensoryRules.iopSuspect(iopMmhg);
    this.fundus=fundus; this.visualField=visualField; this.examiner=examiner;
  }
  public UUID getId(){return id;} public boolean isIopSuspect(){return iopSuspect;}
}
