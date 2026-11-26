package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "encounters")
public class Encounter {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(nullable = false) private String type = "outpatient"; // emergency, outpatient, inpatient, telemedicine, home
  @Column(name = "chief_complaint") private String chiefComplaint;
  @Column(name = "triage_level") private Integer triageLevel; // 1..5
  @Column(name = "news2_score") private Integer news2Score;
  @Column(nullable = false) private String status = "open"; // open, in_progress, completed, closed, cancelled
  @Column(name = "arrived_at") private OffsetDateTime arrivedAt = OffsetDateTime.now();
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Encounter() {}
  public Encounter(UUID facilityId, String governorate, UUID patientId, String type, String chiefComplaint, Integer triageLevel, Integer news2Score) {
    this.facilityId = facilityId; this.governorate = governorate; this.patientId = patientId;
    this.type = type == null ? "outpatient" : type; this.chiefComplaint = chiefComplaint;
    this.triageLevel = triageLevel; this.news2Score = news2Score;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public UUID getPatientId() { return patientId; } public String getType() { return type; }
  public String getChiefComplaint() { return chiefComplaint; } public Integer getTriageLevel() { return triageLevel; }
  public OffsetDateTime getCreatedAt() { return createdAt; } public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public Integer getNews2Score() { return news2Score; } public String getStatus() { return status; }
  public OffsetDateTime getArrivedAt() { return arrivedAt; }
  public void setStatus(String s) { this.status = s; this.updatedAt = OffsetDateTime.now(); }
  public void applyTriage(Integer level, Integer news2, String complaint) {
    if (level != null) this.triageLevel = level;
    if (news2 != null) this.news2Score = news2;
    if (complaint != null && !complaint.isBlank()) this.chiefComplaint = complaint;
    this.updatedAt = OffsetDateTime.now();
  }
}
