package eg.gov.health.platform.patientrights;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "complaint_appeals")
public class ComplaintAppeal {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="complaint_id", nullable=false) private UUID complaintId;
  private String grounds; @Column(name="reviewed_by") private String reviewedBy;
  private String outcome; @Column(nullable=false) private String status = "filed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ComplaintAppeal() {}
  public ComplaintAppeal(UUID facilityId, String governorate, UUID complaintId, String grounds) {
    this.facilityId=facilityId; this.governorate=governorate; this.complaintId=complaintId; this.grounds=grounds;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getOutcome(){return outcome;}
  public void review(String by, String outcome){ this.reviewedBy=by; this.outcome=outcome; this.status=outcome; this.updatedAt=OffsetDateTime.now(); }
}
