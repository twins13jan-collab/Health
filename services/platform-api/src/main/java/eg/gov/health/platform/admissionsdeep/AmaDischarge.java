package eg.gov.health.platform.admissionsdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ama_discharges")
public class AmaDischarge {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="admission_id", nullable=false) private UUID admissionId;
  @Column(name="risks_explained", nullable=false) private boolean risksExplained = false;
  @Column(nullable=false) private boolean signed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AmaDischarge() {}
  public AmaDischarge(UUID facilityId, String governorate, UUID admissionId, boolean risksExplained, boolean signed) {
    this.facilityId=facilityId; this.governorate=governorate; this.admissionId=admissionId; this.risksExplained=risksExplained; this.signed=signed;
  }
  public UUID getId(){return id;}
}
