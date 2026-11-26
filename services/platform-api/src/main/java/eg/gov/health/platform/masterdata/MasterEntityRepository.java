package eg.gov.health.platform.masterdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MasterEntityRepository extends JpaRepository<MasterEntity, UUID> {
  Optional<MasterEntity> findByEntityTypeAndCode(String entityType, String code);
}
