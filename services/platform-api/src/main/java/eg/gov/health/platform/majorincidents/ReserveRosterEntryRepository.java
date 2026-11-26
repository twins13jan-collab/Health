package eg.gov.health.platform.majorincidents;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ReserveRosterEntryRepository extends JpaRepository<ReserveRosterEntry, UUID> {
}
