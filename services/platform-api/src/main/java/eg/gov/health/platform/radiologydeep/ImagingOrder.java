package eg.gov.health.platform.radiologydeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "imaging_orders")
public class ImagingOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String exam; private String justification; private String priority;
  @Column(name="contraindications_checked", nullable=false) private boolean contraindicationsChecked = false;
  @Column(name="contrast_planned", nullable=false) private boolean contrastPlanned = false;
  @Column(name="egfr_value") private Double egfrValue;
  @Column(name="nephrology_override", nullable=false) private boolean nephrologyOverride = false;
  @Column(nullable=false) private String status = "ordered";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ImagingOrder() {}
  public ImagingOrder(UUID facilityId, String governorate, String patientRef, String exam, String justification, String priority,
                        boolean contraindicationsChecked, boolean contrastPlanned, Double egfrValue, boolean nephrologyOverride) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.exam=exam; this.justification=justification;
    this.priority=priority; this.contraindicationsChecked=contraindicationsChecked; this.contrastPlanned=contrastPlanned;
    this.egfrValue=egfrValue; this.nephrologyOverride=nephrologyOverride;
  }
  public UUID getId(){return id;} public boolean isContraindicationsChecked(){return contraindicationsChecked;}
  public boolean isContrastPlanned(){return contrastPlanned;} public Double getEgfrValue(){return egfrValue;}
  public boolean isNephrologyOverride(){return nephrologyOverride;}
  public void perform(){ this.status="performed"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
