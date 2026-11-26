package eg.gov.health.platform.mobiledevices;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PushReceiptRepository extends JpaRepository<PushReceipt, UUID> {
}
