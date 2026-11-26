package eg.gov.health.platform.common;
import org.springframework.stereotype.Service; import java.util.UUID;
@Service
public class AuditService {
  private final AuditLogRepository repo;
  public AuditService(AuditLogRepository repo) { this.repo = repo; }
  public void record(String action, String entityType, String entityId, String payloadJson) {
    var p = TenantContext.get();
    UUID fac = null;
    try { if (p != null && p.facilityId() != null) fac = UUID.fromString(p.facilityId()); } catch (IllegalArgumentException ignored) {}
    repo.save(new AuditLog(fac, p == null ? null : p.governorate(), p == null ? null : p.userId(),
      p == null ? null : p.role(), action, entityType, entityId, payloadJson == null ? "{}" : payloadJson));
  }
}
