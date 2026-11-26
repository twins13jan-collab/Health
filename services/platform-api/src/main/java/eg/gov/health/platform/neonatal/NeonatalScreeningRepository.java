package eg.gov.health.platform.neonatal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NeonatalScreeningRepository extends JpaRepository<NeonatalScreening, UUID> {
  List<NeonatalScreening> findByNewbornIdOrderByCreatedAtDesc(UUID newbornId);
}
