package eg.gov.health.platform.protocolquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ProtocolDeviationRepository extends JpaRepository<ProtocolDeviation, UUID> {
  java.util.List<ProtocolDeviation> findByProtocolId(UUID protocolId);
}
