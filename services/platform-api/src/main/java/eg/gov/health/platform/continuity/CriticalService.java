package eg.gov.health.platform.continuity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "critical_services")
public class CriticalService {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="impact_level") private String impactLevel;
  @Column(name="rto_minutes") private Integer rtoMinutes;
  @Column(name="rpo_minutes") private Integer rpoMinutes;
  @Column(name="backup_configured", nullable=false) private boolean backupConfigured = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CriticalService() {}
  public CriticalService(UUID facilityId, String governorate, String name, String impactLevel, Integer rtoMinutes, Integer rpoMinutes, boolean backupConfigured) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.impactLevel=impactLevel; this.rtoMinutes=rtoMinutes;
    this.rpoMinutes=rpoMinutes; this.backupConfigured=backupConfigured;
  }
  public UUID getId(){return id;} public Integer getRtoMinutes(){return rtoMinutes;} public Integer getRpoMinutes(){return rpoMinutes;}
}
