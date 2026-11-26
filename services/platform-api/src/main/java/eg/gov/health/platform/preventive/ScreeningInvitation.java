package eg.gov.health.platform.preventive;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "screening_invitations")
public class ScreeningInvitation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="program_id", nullable=false) private UUID programId;
  @Column(name="person_ref") private String personRef;
  @Column(nullable=false) private boolean eligible = false;
  @Column(name="consent_given", nullable=false) private boolean consentGiven = false;
  @Column(name="appointment_at") private OffsetDateTime appointmentAt;
  @Column(nullable=false) private String attendance = "invited";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ScreeningInvitation() {}
  public ScreeningInvitation(UUID facilityId, String governorate, UUID programId, String personRef, boolean eligible, boolean consentGiven, OffsetDateTime appointmentAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.programId=programId; this.personRef=personRef; this.eligible=eligible;
    this.consentGiven=consentGiven; this.appointmentAt=appointmentAt;
  }
  public UUID getId(){return id;} public boolean isEligible(){return eligible;} public boolean isConsentGiven(){return consentGiven;} public String getAttendance(){return attendance;}
  public void setAttendance(String a){ this.attendance=a; this.updatedAt=OffsetDateTime.now(); }
}
