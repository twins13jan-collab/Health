package eg.gov.health.platform.nursing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NursingRoundRepository extends JpaRepository<NursingRound, UUID> {
  List<NursingRound> findByAdmissionIdOrderByRoundAtDesc(UUID admissionId);
  List<NursingRound> findByPatientIdOrderByRoundAtDesc(UUID patientId);
  Optional<NursingRound> findFirstByAdmissionIdOrderByRoundAtDesc(UUID admissionId);
}
