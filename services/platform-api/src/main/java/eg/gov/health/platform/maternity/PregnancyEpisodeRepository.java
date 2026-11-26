package eg.gov.health.platform.maternity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PregnancyEpisodeRepository extends JpaRepository<PregnancyEpisode, UUID> {
  List<PregnancyEpisode> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
  List<PregnancyEpisode> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}
