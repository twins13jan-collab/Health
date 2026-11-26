package eg.gov.health.platform.criticalcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IcuStayRepository extends JpaRepository<IcuStay, UUID> {
  List<IcuStay> findByPatientIdOrderByAdmittedAtDesc(UUID patientId);
  List<IcuStay> findByFacilityIdAndStatus(UUID facilityId, String status);
}
