package eg.gov.health.platform.occhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface OccupationalReferralRepository extends JpaRepository<OccupationalReferral, UUID> {
}
