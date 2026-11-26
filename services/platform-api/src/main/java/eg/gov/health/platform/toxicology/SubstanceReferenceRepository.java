package eg.gov.health.platform.toxicology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SubstanceReferenceRepository extends JpaRepository<SubstanceReference, UUID> {
  List<SubstanceReference> findByNameContainingIgnoreCase(String name);
}
