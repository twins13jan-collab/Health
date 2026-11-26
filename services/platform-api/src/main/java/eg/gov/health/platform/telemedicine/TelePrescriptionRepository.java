package eg.gov.health.platform.telemedicine;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TelePrescriptionRepository extends JpaRepository<TelePrescription, UUID> {}
