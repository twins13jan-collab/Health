package eg.gov.health.platform.workforce;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {
  List<StaffMember> findByFacilityIdOrderByFullName(UUID facilityId);
}
