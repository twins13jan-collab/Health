package eg.gov.health.platform.servicedesk;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {}
