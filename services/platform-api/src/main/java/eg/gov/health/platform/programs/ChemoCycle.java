package eg.gov.health.platform.programs;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import java.util.UUID;
@Entity @Table(name = "chemo_cycles")
public class ChemoCycle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String protocol;
  @Column(name="drug_code", nullable=false) private String drugCode;
  @Column(name="cycle_number", nullable=false) private int cycleNumber;
  @Column(name="bsa", nullable=false) private Double bsa;
  @Column(name="dose_mg", nullable=false) private BigDecimal doseMg;
  @Column(name="scheduled_date") private LocalDate scheduledDate;
  @Column(nullable=false) private String status = "planned"; // planned, verified, administered, held, cancelled
  @Column(name="verified_by") private String verifiedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ChemoCycle() {}
  public ChemoCycle(UUID facilityId, String governorate, UUID patientId, String protocol, String drugCode, int cycleNumber, Double bsa, BigDecimal doseMg, LocalDate scheduledDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.protocol=protocol;
    this.drugCode=drugCode; this.cycleNumber=cycleNumber; this.bsa=bsa; this.doseMg=doseMg; this.scheduledDate=scheduledDate;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public BigDecimal getDoseMg(){return doseMg;}
  public void verify(String by){this.verifiedBy=by; this.status="verified"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
