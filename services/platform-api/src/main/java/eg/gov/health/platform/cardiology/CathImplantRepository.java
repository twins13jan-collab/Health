package eg.gov.health.platform.cardiology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CathImplantRepository extends JpaRepository<CathImplant, UUID> {}
