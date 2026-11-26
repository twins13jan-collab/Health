package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {
  List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
  List<ClinicalNote> findByEncounterIdOrderByCreatedAtAsc(UUID encounterId);
}
