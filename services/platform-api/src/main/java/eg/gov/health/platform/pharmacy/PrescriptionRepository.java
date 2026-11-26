package eg.gov.health.platform.pharmacy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
  List<Prescription> findByPatientIdAndStatusIn(UUID patientId, List<String> statuses);
  List<Prescription> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}
