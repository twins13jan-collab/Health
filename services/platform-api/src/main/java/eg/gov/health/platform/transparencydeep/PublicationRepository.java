package eg.gov.health.platform.transparencydeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PublicationRepository extends JpaRepository<Publication, UUID> {}
