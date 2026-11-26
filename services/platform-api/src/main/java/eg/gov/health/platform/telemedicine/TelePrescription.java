package eg.gov.health.platform.telemedicine;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "tele_prescriptions")
public class TelePrescription {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="session_id", nullable=false) private UUID sessionId;
  private String drug;
  @Column(name="regulation_compliant", nullable=false) private boolean regulationCompliant = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TelePrescription() {}
  public TelePrescription(UUID facilityId, String governorate, UUID sessionId, String drug, boolean regulationCompliant) {
    this.facilityId=facilityId; this.governorate=governorate; this.sessionId=sessionId; this.drug=drug; this.regulationCompliant=regulationCompliant;
  }
  public UUID getId(){return id;}
}
