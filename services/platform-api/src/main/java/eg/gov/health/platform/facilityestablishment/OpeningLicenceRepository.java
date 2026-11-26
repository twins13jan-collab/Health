package eg.gov.health.platform.facilityestablishment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface OpeningLicenceRepository extends JpaRepository<OpeningLicence, UUID> {
  java.util.List<OpeningLicence> findByProjectId(UUID projectId);
}
