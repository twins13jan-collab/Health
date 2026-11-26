package eg.gov.health.platform.schoolhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "school_screenings")
public class SchoolScreening {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="student_id", nullable=false) private UUID studentId;
  @Column(name="screening_type") private String screeningType;
  private String result;
  @Column(name="referral_needed", nullable=false) private boolean referralNeeded = false;
  @Column(name="referral_ref") private String referralRef;
  @Column(name="guardian_notified", nullable=false) private boolean guardianNotified = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SchoolScreening() {}
  public SchoolScreening(UUID facilityId, String governorate, UUID studentId, String screeningType, String result,
                         boolean referralNeeded, String referralRef, boolean guardianNotified) {
    this.facilityId=facilityId; this.governorate=governorate; this.studentId=studentId; this.screeningType=screeningType; this.result=result;
    this.referralNeeded=referralNeeded; this.referralRef=referralRef; this.guardianNotified=guardianNotified;
  }
  public UUID getId(){return id;}
}
