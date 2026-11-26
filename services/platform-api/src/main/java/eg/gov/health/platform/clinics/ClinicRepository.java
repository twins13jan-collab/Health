package eg.gov.health.platform.clinics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ClinicRepository extends JpaRepository<Clinic, UUID> {
  List<Clinic> findByFacilityIdAndActiveTrueOrderByNameArAsc(UUID facilityId);
  List<Clinic> findByFacilityIdOrderByNameArAsc(UUID facilityId);
}
