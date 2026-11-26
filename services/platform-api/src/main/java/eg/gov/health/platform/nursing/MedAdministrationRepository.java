package eg.gov.health.platform.nursing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MedAdministrationRepository extends JpaRepository<MedAdministration, UUID> {
  boolean existsByPrescriptionIdAndScheduledAt(UUID prescriptionId, OffsetDateTime scheduledAt);
  List<MedAdministration> findByPatientIdAndScheduledAtBetweenOrderByScheduledAtAsc(UUID patientId, OffsetDateTime from, OffsetDateTime to);
  List<MedAdministration> findByFacilityIdAndScheduledAtBetweenOrderByScheduledAtAsc(UUID facilityId, OffsetDateTime from, OffsetDateTime to);
  List<MedAdministration> findByAdmissionIdOrderByScheduledAtAsc(UUID admissionId);
}
