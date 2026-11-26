package eg.gov.health.platform.journey;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface JourneyStepRepository extends JpaRepository<JourneyStep, UUID> {
  java.util.List<JourneyStep> findByJourneyIdOrderByCreatedAtAsc(UUID journeyId);
  boolean existsByJourneyIdAndEntityId(UUID journeyId, UUID entityId);
}
