package eg.gov.health.platform.outpatientdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ClinicNoShowRepository extends JpaRepository<ClinicNoShow, UUID> {}
