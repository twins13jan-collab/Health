package eg.gov.health.platform.medicalassets;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "calibration_records")
public class CalibrationRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  @Column(name="reference_standard") private String referenceStandard;
  @Column(name="certificate_ref") private String certificateRef;
  private String deviation; @Column(nullable=false) private boolean accepted = false;
  @Column(name="due_date") private LocalDate dueDate;
  @Column(name="approved_by") private String approvedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CalibrationRecord() {}
  public CalibrationRecord(UUID facilityId, String governorate, UUID assetId, String referenceStandard, String certificateRef, String deviation, boolean accepted, LocalDate dueDate, String approvedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetId=assetId; this.referenceStandard=referenceStandard;
    this.certificateRef=certificateRef; this.deviation=deviation; this.accepted=accepted; this.dueDate=dueDate; this.approvedBy=approvedBy;
  }
  public UUID getId(){return id;} public boolean isAccepted(){return accepted;}
}
