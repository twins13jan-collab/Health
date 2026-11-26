package eg.gov.health.platform.citizenportal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreference, UUID> {
  java.util.Optional<CommunicationPreference> findByAccountIdAndChannel(UUID accountId, String channel);
}
