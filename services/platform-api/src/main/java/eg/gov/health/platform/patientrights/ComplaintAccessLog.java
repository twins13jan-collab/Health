package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "complaint_access_log")
public class ComplaintAccessLog {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complaint_id", nullable=false) private UUID complaintId;
  private String accessor; private String purpose;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ComplaintAccessLog() {}
  public ComplaintAccessLog(UUID facilityId, String governorate, UUID complaintId, String accessor, String purpose) {
    this.facilityId=facilityId; this.governorate=governorate; this.complaintId=complaintId; this.accessor=accessor; this.purpose=purpose;
  }
  public UUID getId(){return id;}
}
