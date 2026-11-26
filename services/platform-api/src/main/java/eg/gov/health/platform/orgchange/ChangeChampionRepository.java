package eg.gov.health.platform.orgchange;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ChangeChampionRepository extends JpaRepository<ChangeChampion, UUID> {
}
