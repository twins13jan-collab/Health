package eg.gov.health.platform.facilityestablishment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EstablishmentProjectRepository extends JpaRepository<EstablishmentProject, UUID> {
}
