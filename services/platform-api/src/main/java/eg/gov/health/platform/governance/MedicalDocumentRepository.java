package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, UUID> {
  List<MedicalDocument> findByPatientIdOrderByDocumentDateDesc(UUID patientId);
}
