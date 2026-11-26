package eg.gov.health.platform.nursing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface NursingTaskRepository extends JpaRepository<NursingTask, UUID> {
  long countByAssignedToAndStatusIn(String assignedTo, java.util.List<String> statuses);
  java.util.List<NursingTask> findByPatientRefAndStatus(String patientRef, String status);
}
