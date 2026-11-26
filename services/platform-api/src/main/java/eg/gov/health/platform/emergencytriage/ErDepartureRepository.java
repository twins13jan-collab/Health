package eg.gov.health.platform.emergencytriage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ErDepartureRepository extends JpaRepository<ErDeparture, UUID> {}
