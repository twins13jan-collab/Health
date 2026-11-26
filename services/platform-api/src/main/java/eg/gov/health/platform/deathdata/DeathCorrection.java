package eg.gov.health.platform.deathdata;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "death_corrections")
public class DeathCorrection {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="registration_id", nullable=false) private UUID registrationId;
  private String field;
  @Column(name="old_value") private String oldValue;
  @Column(name="new_value") private String newValue;
  private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DeathCorrection() {}
  public DeathCorrection(UUID facilityId, String governorate, UUID registrationId, String field, String oldValue, String newValue, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.registrationId=registrationId; this.field=field;
    this.oldValue=oldValue; this.newValue=newValue; this.reason=reason;
  }
  public UUID getId(){return id;} public String getField(){return field;} public String getOldValue(){return oldValue;}
  public String getNewValue(){return newValue;} public String getReason(){return reason;}
}
