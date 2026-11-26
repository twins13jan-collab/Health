package eg.gov.health.platform.finance;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "invoices")
public class Invoice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="total_amount", nullable=false) private BigDecimal totalAmount = BigDecimal.ZERO;
  @Column(name="insurer_amount", nullable=false) private BigDecimal insurerAmount = BigDecimal.ZERO;
  @Column(name="patient_amount", nullable=false) private BigDecimal patientAmount = BigDecimal.ZERO;
  @Column(nullable=false) private String status = "draft"; // draft, issued, claimed, partially_paid, paid, rejected, cancelled
  @Column(name="payer") private String payer;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Invoice() {}
  public Invoice(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String payer) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId; this.payer=payer;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public BigDecimal getTotalAmount(){return totalAmount;}
  public BigDecimal getInsurerAmount(){return insurerAmount;} public BigDecimal getPatientAmount(){return patientAmount;}
  public UUID getFacilityId(){return facilityId;} public UUID getPatientId()
  {return patientId;}
  public void setTotals(BigDecimal t, BigDecimal i, BigDecimal p){this.totalAmount=t; this.insurerAmount=i; this.patientAmount=p; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}

  public String getPayer(){return payer;} public java.time.OffsetDateTime getCreatedAt(){return createdAt;} public UUID getEncounterId(){return encounterId;}
}
