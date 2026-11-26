package eg.gov.health.platform.schoolhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SchoolVaccinationRepository extends JpaRepository<SchoolVaccination, UUID> {
}
