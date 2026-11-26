package eg.gov.health.platform.chronicdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ChronicEnrollmentRepository extends JpaRepository<ChronicEnrollment, UUID> {
}
