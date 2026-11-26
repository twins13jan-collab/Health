package eg.gov.health.platform.labsafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TestIntroductionRepository extends JpaRepository<TestIntroduction, UUID> {}
