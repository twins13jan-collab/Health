package eg.gov.health.platform.common;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
@Entity @Table(name = "audit_logs")
public class AuditLog {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id") private UUID facilityId;
  private String governorate; private String actorId; private String actorRole;
  private String action; private String entityType; private String entityId;
  @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb") private String payload;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AuditLog() {}
  public AuditLog(UUID facilityId, String governorate, String actorId, String actorRole, String action, String entityType, String entityId, String payload) {
    this.facilityId = facilityId; this.governorate = governorate; this.actorId = actorId; this.actorRole = actorRole;
    this.action = action; this.entityType = entityType; this.entityId = entityId; this.payload = payload;
  }
  public UUID getId() { return id; }
}
