package eg.gov.health.platform.pathology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BlockSlideRepository extends JpaRepository<BlockSlide, UUID> {
  List<BlockSlide> findByAccessionIdOrderByCreatedAtAsc(UUID accessionId);
}
