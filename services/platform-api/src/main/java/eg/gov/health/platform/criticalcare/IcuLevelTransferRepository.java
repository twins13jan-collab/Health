package eg.gov.health.platform.criticalcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface IcuLevelTransferRepository extends JpaRepository<IcuLevelTransfer, UUID> {}
