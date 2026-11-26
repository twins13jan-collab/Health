package eg.gov.health.platform.oncology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "protocol_versions")
public class ProtocolVersion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="regimen_name", nullable=false) private String regimenName;
  @Column(nullable=false) private String version;
  private String conditions; @Column(name="approval_source") private String approvalSource;
  @Column(name="approved_at") private LocalDate approvedAt;
  @Column(name="valid_until") private LocalDate validUntil;
  @Column(name="medically_approved", nullable=false) private boolean medicallyApproved = false;
  @Column(name="max_weight_change_pct") private java.math.BigDecimal maxWeightChangePct;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ProtocolVersion() {}
  public ProtocolVersion(UUID facilityId, String governorate, String regimenName, String version, String conditions, String approvalSource,
                         LocalDate approvedAt, LocalDate validUntil, boolean medicallyApproved, java.math.BigDecimal maxWeightChangePct) {
    this.facilityId=facilityId; this.governorate=governorate; this.regimenName=regimenName; this.version=version; this.conditions=conditions;
    this.approvalSource=approvalSource; this.approvedAt=approvedAt; this.validUntil=validUntil; this.medicallyApproved=medicallyApproved;
    this.maxWeightChangePct=maxWeightChangePct;
  }
  public UUID getId(){return id;} public boolean isMedicallyApproved(){return medicallyApproved;}
  public LocalDate getValidUntil(){return validUntil;} public String getStatus(){return status;}
  public java.math.BigDecimal getMaxWeightChangePct(){return maxWeightChangePct;}
}
