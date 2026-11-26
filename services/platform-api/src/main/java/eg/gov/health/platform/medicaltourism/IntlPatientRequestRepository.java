package eg.gov.health.platform.medicaltourism;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface IntlPatientRequestRepository extends JpaRepository<IntlPatientRequest, UUID> {}
