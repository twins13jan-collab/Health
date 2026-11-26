package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AcknowledgementRepository extends JpaRepository<Acknowledgement, UUID> {
  List<Acknowledgement> findByMessageRefOrderByCreatedAtAsc(UUID messageRef);
}
