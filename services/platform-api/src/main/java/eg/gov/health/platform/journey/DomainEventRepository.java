package eg.gov.health.platform.journey;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DomainEventRepository extends JpaRepository<DomainEvent, UUID> {
  java.util.List<DomainEvent> findByPublishedAtIsNullOrderByCreatedAtAsc();
  java.util.List<DomainEvent> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
}
