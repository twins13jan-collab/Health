package eg.gov.health.platform.organbank;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "organ_discard_records")
public class OrganDiscardRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="donation_id", nullable=false) private UUID donationId;
  private String reason; @Column(name="investigated_by") private String investigatedBy;
  private String investigation;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected OrganDiscardRecord() {}
  public OrganDiscardRecord(UUID facilityId, String governorate, UUID donationId, String reason, String investigatedBy, String investigation) {
    this.facilityId=facilityId; this.governorate=governorate; this.donationId=donationId; this.reason=reason;
    this.investigatedBy=investigatedBy; this.investigation=investigation;
  }
  public UUID getId(){return id;}
}
