package eg.gov.health.platform.amrdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CultureResultRepository extends JpaRepository<CultureResult, UUID> {
  java.util.List<CultureResult> findByOrganismAndAntibiotic(String organism, String antibiotic);
}
