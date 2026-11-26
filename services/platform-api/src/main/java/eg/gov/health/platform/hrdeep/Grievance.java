package eg.gov.health.platform.hrdeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "grievances")
public class Grievance {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="staff_ref") private String staffRef;
  private String subject;
  @Column(nullable=false) private boolean reviewed = false;
  private String outcome;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Grievance() {}
  public Grievance(UUID facilityId, String governorate, String staffRef, String subject, boolean reviewed, String outcome) {
    this.facilityId=facilityId; this.governorate=governorate; this.staffRef=staffRef; this.subject=subject; this.reviewed=reviewed; this.outcome=outcome;
  }
  public UUID getId(){return id;} public boolean isReviewed(){return reviewed;} public String getOutcome(){return outcome;}
  public String getStaffRef(){return staffRef;} public String getSubject(){return subject;}
  public String getStatus(){return status;} public OffsetDateTime getCreatedAt(){return createdAt;}
  public UUID getFacilityId(){return facilityId;}
  public void review(String outcome){ this.reviewed=true; this.outcome=outcome; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
