package eg.gov.health.platform.masterdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MappingSetRepository extends JpaRepository<MappingSet, UUID> {
  List<MappingSet> findByLocalCode(String localCode);
}
