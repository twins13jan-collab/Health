package eg.gov.health.platform.programs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dialysis_sessions")
public class DialysisSession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="machine_code", nullable=false) private String machineCode;
  @Column(name="starts_at", nullable=false) private OffsetDateTime startsAt;
  @Column(name="ends_at", nullable=false) private OffsetDateTime endsAt;
  @Column(name="dry_weight_kg") private Double dryWeightKg;
  @Column(name="ultrafiltration_ml") private Integer ultrafiltrationMl;
  @Column(nullable=false) private String status = "scheduled"; // scheduled, in_session, completed, missed, cancelled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DialysisSession() {}
  public DialysisSession(UUID facilityId, String governorate, UUID patientId, String machineCode, OffsetDateTime startsAt, OffsetDateTime endsAt, Double dryWeightKg, Integer uf) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.machineCode=machineCode;
    this.startsAt=startsAt; this.endsAt=endsAt; this.dryWeightKg=dryWeightKg; this.ultrafiltrationMl=uf;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;}
  public OffsetDateTime getStartsAt(){return startsAt;} public OffsetDateTime getEndsAt(){return endsAt;}
  public String getMachineCode(){return machineCode;}
  public boolean overlaps(DialysisSession o) {
    return machineCode.equals(o.machineCode) && !"cancelled".equals(o.status) && startsAt.isBefore(o.endsAt) && o.startsAt.isBefore(endsAt);
  }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
