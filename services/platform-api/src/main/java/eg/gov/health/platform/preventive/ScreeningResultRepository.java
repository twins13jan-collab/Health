package eg.gov.health.platform.preventive;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ScreeningResultRepository extends JpaRepository<ScreeningResult, UUID> {
}
