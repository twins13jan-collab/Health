package eg.gov.health.platform.clinicalriskmatrix;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MatrixAlertRuleRepository extends JpaRepository<MatrixAlertRule, UUID> {}
