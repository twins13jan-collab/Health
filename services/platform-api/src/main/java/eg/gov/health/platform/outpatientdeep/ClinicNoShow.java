package eg.gov.health.platform.outpatientdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "clinic_no_shows")
public class ClinicNoShow {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="visit_id", nullable=false) private UUID visitId;
  @Column(name="reschedule_offered", nullable=false) private boolean rescheduleOffered = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClinicNoShow() {}
  public ClinicNoShow(UUID facilityId, String governorate, UUID visitId, boolean rescheduleOffered) {
    this.facilityId=facilityId; this.governorate=governorate; this.visitId=visitId; this.rescheduleOffered=rescheduleOffered;
  }
  public UUID getId(){return id;}
}
