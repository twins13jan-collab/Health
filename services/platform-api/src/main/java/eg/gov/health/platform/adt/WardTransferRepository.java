package eg.gov.health.platform.adt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WardTransferRepository extends JpaRepository<WardTransfer, UUID> {
  List<WardTransfer> findByAdmissionIdOrderByCreatedAtAsc(UUID admissionId);
}
