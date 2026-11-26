package eg.gov.health.platform.citizenportal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface CitizenAccountRepository extends JpaRepository<CitizenAccount, UUID> {
}
