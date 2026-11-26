package eg.gov.health.platform.occhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface OccupationalExaminationRepository extends JpaRepository<OccupationalExamination, UUID> {
  java.util.List<OccupationalExamination> findByWorkerIdOrderByExaminedAtDesc(UUID workerId);
}
