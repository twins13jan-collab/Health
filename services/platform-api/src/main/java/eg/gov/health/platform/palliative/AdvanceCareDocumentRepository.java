package eg.gov.health.platform.palliative;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AdvanceCareDocumentRepository extends JpaRepository<AdvanceCareDocument, UUID> {
  List<AdvanceCareDocument> findByPatientIdAndStatus(UUID patientId, String status);
}
