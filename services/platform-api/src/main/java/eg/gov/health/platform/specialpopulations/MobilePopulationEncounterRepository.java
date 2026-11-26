package eg.gov.health.platform.specialpopulations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MobilePopulationEncounterRepository extends JpaRepository<MobilePopulationEncounter, UUID> {}
