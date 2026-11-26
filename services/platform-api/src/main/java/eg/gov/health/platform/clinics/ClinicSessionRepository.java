package eg.gov.health.platform.clinics;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface ClinicSessionRepository extends JpaRepository<ClinicSession, UUID> {
  List<ClinicSession> findByFacilityIdAndSessionDateOrderByStartsAtAsc(UUID facilityId, LocalDate date);
  List<ClinicSession> findByClinicIdAndSessionDateOrderByStartsAtAsc(UUID clinicId, LocalDate date);
}
