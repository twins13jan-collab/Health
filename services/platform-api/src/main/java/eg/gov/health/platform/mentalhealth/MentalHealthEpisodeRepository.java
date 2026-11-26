package eg.gov.health.platform.mentalhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MentalHealthEpisodeRepository extends JpaRepository<MentalHealthEpisode, UUID> {
  List<MentalHealthEpisode> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
