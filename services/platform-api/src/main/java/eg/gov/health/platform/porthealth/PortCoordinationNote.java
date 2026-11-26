package eg.gov.health.platform.porthealth;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "port_coordination_notes")
public class PortCoordinationNote {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="report_id", nullable=false) private UUID reportId;
  private String authority; private String note;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PortCoordinationNote() {}
  public PortCoordinationNote(UUID facilityId, String governorate, UUID reportId, String authority, String note) {
    this.facilityId=facilityId; this.governorate=governorate; this.reportId=reportId; this.authority=authority; this.note=note;
  }
  public UUID getId(){return id;}
}
