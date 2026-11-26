package eg.gov.health.platform.majorincidents;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ReadinessExerciseRepository extends JpaRepository<ReadinessExercise, UUID> {
  java.util.Optional<ReadinessExercise> findFirstByPlanIdOrderByConductedAtDesc(UUID planId);
}
