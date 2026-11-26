package eg.gov.health.platform.maternity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AntenatalVisitRepository extends JpaRepository<AntenatalVisit, UUID> {
  List<AntenatalVisit> findByPregnancyIdOrderByVisitDateDesc(UUID pregnancyId);
}
