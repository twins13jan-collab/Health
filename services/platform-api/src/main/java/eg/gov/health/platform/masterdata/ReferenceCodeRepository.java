package eg.gov.health.platform.masterdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ReferenceCodeRepository extends JpaRepository<ReferenceCode, UUID> {
  Optional<ReferenceCode> findByCodeSystemAndCode(String codeSystem, String code);
  List<ReferenceCode> findByStatusOrderByCreatedAtAsc(String status);
}
