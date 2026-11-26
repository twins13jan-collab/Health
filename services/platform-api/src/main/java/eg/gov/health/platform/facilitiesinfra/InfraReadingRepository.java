package eg.gov.health.platform.facilitiesinfra;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface InfraReadingRepository extends JpaRepository<InfraReading, UUID> {
  java.util.List<InfraReading> findBySystemIdOrderByCreatedAtDesc(UUID systemId);
}
