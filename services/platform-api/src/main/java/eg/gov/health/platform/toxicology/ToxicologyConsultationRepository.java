package eg.gov.health.platform.toxicology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ToxicologyConsultationRepository extends JpaRepository<ToxicologyConsultation, UUID> {
  List<ToxicologyConsultation> findByCaseIdOrderByContactTimeDesc(UUID caseId);
}
