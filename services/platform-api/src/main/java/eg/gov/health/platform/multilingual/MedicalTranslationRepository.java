package eg.gov.health.platform.multilingual;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MedicalTranslationRepository extends JpaRepository<MedicalTranslation, UUID> {}
