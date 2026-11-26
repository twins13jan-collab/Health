package eg.gov.health.platform.diseaseregistries;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DiseaseRegistryRepository extends JpaRepository<DiseaseRegistry, UUID> {
}
