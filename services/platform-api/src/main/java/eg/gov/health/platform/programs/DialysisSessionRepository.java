package eg.gov.health.platform.programs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DialysisSessionRepository extends JpaRepository<DialysisSession, UUID> {
  List<DialysisSession> findByFacilityIdAndMachineCode(UUID facilityId, String machineCode);
  List<DialysisSession> findByPatientIdOrderByStartsAtAsc(UUID patientId);
}
