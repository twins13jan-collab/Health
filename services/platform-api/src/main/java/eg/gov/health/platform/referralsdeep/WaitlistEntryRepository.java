package eg.gov.health.platform.referralsdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {
  java.util.List<WaitlistEntry> findByServiceRef(String serviceRef);
}
