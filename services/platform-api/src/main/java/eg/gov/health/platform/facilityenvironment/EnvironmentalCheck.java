package eg.gov.health.platform.facilityenvironment;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "environmental_checks")
public class EnvironmentalCheck {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="check_type", nullable=false) private String checkType;
  private String location; private String result;
  @Column(name="measured_value") private String measuredValue;
  @Column(name="standard_ref") private String standardRef;
  @Column(name="checked_by") private String checkedBy;
  @Column(name="action_taken") private String actionTaken;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected EnvironmentalCheck() {}
  public EnvironmentalCheck(UUID facilityId, String governorate, String checkType, String location, String result, String measuredValue, String standardRef, String checkedBy, String actionTaken) {
    this.facilityId=facilityId; this.governorate=governorate; this.checkType=checkType; this.location=location; this.result=result;
    this.measuredValue=measuredValue; this.standardRef=standardRef; this.checkedBy=checkedBy; this.actionTaken=actionTaken;
  }
  public UUID getId(){return id;}
}
