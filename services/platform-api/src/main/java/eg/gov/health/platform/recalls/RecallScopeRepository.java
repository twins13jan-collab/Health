package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RecallScopeRepository extends JpaRepository<RecallScope, UUID> {
  List<RecallScope> findByNoticeId(UUID noticeId);
}
