package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RecallReceiptRepository extends JpaRepository<RecallReceipt, UUID> {
  List<RecallReceipt> findByNoticeId(UUID noticeId);
}
