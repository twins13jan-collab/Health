package eg.gov.health.platform.journey;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CareJourneyRepository extends JpaRepository<CareJourney, UUID> {
}
