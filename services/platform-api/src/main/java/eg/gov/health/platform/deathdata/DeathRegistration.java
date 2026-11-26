package eg.gov.health.platform.deathdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "death_registrations")
public class DeathRegistration {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="deceased_ref") private String deceasedRef;
  private String place; private String source;
  @Column(name="cause_code") private String causeCode;
  @Column(name="cause_certified", nullable=false) private boolean causeCertified = false;
  @Column(name="statistics_linked", nullable=false) private boolean statisticsLinked = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeathRegistration() {}
  public DeathRegistration(UUID facilityId, String governorate, String deceasedRef, String place, String source, String causeCode, boolean causeCertified) {
    this.facilityId=facilityId; this.governorate=governorate; this.deceasedRef=deceasedRef; this.place=place; this.source=source;
    this.causeCode=causeCode; this.causeCertified=causeCertified;
  }
  public UUID getId(){return id;} public String getCauseCode(){return causeCode;} public boolean isCauseCertified(){return causeCertified;}
  public String getPlace(){return place;}
  public void linkStatistics(){ this.statisticsLinked=true; this.updatedAt=OffsetDateTime.now(); }
  public void correctPlace(String newPlace){ this.place=newPlace; this.updatedAt=OffsetDateTime.now(); }
  public void correctCause(String newCause){ this.causeCode=newCause; this.updatedAt=OffsetDateTime.now(); }
}
