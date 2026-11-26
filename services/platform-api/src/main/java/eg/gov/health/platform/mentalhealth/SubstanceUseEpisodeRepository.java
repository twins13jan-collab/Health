package eg.gov.health.platform.mentalhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SubstanceUseEpisodeRepository extends JpaRepository<SubstanceUseEpisode, UUID> {
  List<SubstanceUseEpisode> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
