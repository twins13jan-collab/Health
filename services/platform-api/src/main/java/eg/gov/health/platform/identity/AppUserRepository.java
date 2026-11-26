package eg.gov.health.platform.identity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
  Optional<AppUser> findByEmail(String email);
  java.util.List<AppUser> findByFacilityIdAndActiveFalse(UUID facilityId);
  java.util.List<AppUser> findByActiveFalse();
  boolean existsByEmail(String email);
  org.springframework.data.domain.Page<AppUser> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
      String email, String fullName, org.springframework.data.domain.Pageable pageable);
}
