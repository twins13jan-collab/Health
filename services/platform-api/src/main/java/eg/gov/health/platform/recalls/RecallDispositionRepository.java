package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RecallDispositionRepository extends JpaRepository<RecallDisposition, UUID> {
  List<RecallDisposition> findByNoticeId(UUID noticeId);
}
