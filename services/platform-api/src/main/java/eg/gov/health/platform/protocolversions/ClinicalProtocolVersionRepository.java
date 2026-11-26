package eg.gov.health.platform.protocolversions;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ClinicalProtocolVersionRepository extends JpaRepository<ClinicalProtocolVersion, UUID> {
  java.util.List<ClinicalProtocolVersion> findByCodeOrderByEffectiveFromDesc(String code);
}
