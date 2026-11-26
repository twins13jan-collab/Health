package eg.gov.health.platform.surgeryops;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurgicalWaitlistRepository extends JpaRepository<SurgicalWaitlistEntry, UUID> {
  List<SurgicalWaitlistEntry> findByFacilityIdAndStatusOrderByCreatedAtAsc(UUID facilityId, String status);
  Optional<SurgicalWaitlistEntry> findByScheduledCaseId(UUID caseId);
}
