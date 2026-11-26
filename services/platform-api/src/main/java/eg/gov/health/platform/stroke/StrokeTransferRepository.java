package eg.gov.health.platform.stroke;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface StrokeTransferRepository extends JpaRepository<StrokeTransfer, UUID> {}
