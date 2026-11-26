package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
  List<Appointment> findByFacilityIdAndDoctorName(UUID facilityId, String doctorName);
  List<Appointment> findByPatientIdOrderByStartsAtAsc(UUID patientId);
  List<Appointment> findBySessionIdOrderByStartsAtAsc(UUID sessionId);
  long countBySessionIdAndStatusNot(UUID sessionId, String status);
  boolean existsBySessionIdAndPatientIdAndStatusNot(UUID sessionId, UUID patientId, String status);
}
