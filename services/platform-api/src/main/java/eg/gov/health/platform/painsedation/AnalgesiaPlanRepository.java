package eg.gov.health.platform.painsedation;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AnalgesiaPlanRepository extends JpaRepository<AnalgesiaPlan, UUID> {}
