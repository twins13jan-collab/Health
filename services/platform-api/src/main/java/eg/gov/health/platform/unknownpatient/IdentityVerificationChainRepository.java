package eg.gov.health.platform.unknownpatient;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IdentityVerificationChainRepository extends JpaRepository<IdentityVerificationChain, UUID> {
  java.util.List<IdentityVerificationChain> findByTempIdOrderByCreatedAtAsc(UUID tempId);
}
