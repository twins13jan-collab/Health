package eg.gov.health.platform.adt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface InpatientAdmissionRepository extends JpaRepository<InpatientAdmission, UUID> {
  List<InpatientAdmission> findByFacilityIdAndStatusOrderByAdmittedAtAsc(UUID facilityId, String status);
  List<InpatientAdmission> findByWardIdAndStatus(UUID wardId, String status);
  List<InpatientAdmission> findByFacilityIdOrderByAdmittedAtDesc(UUID facilityId);
  long countByFacilityIdAndStatus(UUID facilityId, String status);
  List<InpatientAdmission> findByStatus(String status);
}
