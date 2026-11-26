package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {}
